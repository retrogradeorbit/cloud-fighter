(ns cloud-fighter.enemy
  (:require [infinitelives.utils.vec2 :as vec2]
            [infinitelives.utils.events :as e]
            [infinitelives.utils.boid :as b]
            [infinitelives.utils.math :as math]
            [infinitelives.utils.spatial :as spatial]
            [infinitelives.utils.sound :as sound]
            [infinitelives.utils.console :refer [log]]
            [infinitelives.pixi.sprite :as s]
            [cloud-fighter.state :as state]
            [cloud-fighter.explosion :as explosion]
            [cloud-fighter.bullet :as bullet]
            [cloud-fighter.missile :as missile]
            [cljs.core.async :refer [<! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [infinitelives.pixi.macros :as m]))

(defonce enemies (atom {}))

(defn add! [ekey enemy]
  (swap! enemies assoc ekey enemy))

(defn remove! [ekey]
  (swap! enemies dissoc ekey))

(defn count-enemies []
  (count @enemies))

(defonce enemy-bullets (atom {}))

(defn remove-bullet! [bkey]
  (swap! enemy-bullets dissoc bkey))

(defn spawn-bullet! [canvas initial-pos heading speed lifetime bullet-scale bullet-gfx]
  (let [update (-> heading vec2/unit
                   (vec2/scale speed))
        bkey (keyword (gensym))
        skey [:enemy-bullet bkey]
        ]
    (go
      (sound/play-sound :enemy-shoot 0.5 false)
      (m/with-sprite canvas :clouds-lower
        [bullet (s/make-sprite bullet-gfx :scale bullet-scale :x 0 :y 0)]
        (swap! enemy-bullets assoc bkey bullet)
        (spatial/add-to-spatial! :default skey (vec2/as-vector initial-pos))
        (loop [n lifetime
               pos initial-pos]
          (s/set-pos! bullet pos)
          (<! (e/next-frame))

          (if (and (pos? n) (bkey @enemy-bullets))
            (let [next-pos (vec2/sub (vec2/add pos update) (:vel @state/state))]
              (spatial/move-in-spatial :default skey
                                       (vec2/as-vector pos)
                                       (vec2/as-vector next-pos))
              (recur (dec n) next-pos))

            (do
              (remove! bkey)
              (spatial/remove-from-spatial :default skey (vec2/as-vector pos)))))))))

(defn spawn [canvas]
  (go
    (let [gutter 32
          hw (+ gutter (/ (.-innerWidth js/window) 2))
          hh (+ gutter (/ (.-innerHeight js/window) 2))
          start-pos (case (math/rand-between 1 4)
                      1 ;; top
                      (vec2/vec2 (math/rand-between (- hw) hw) (- hh))

                      2 ;; bottom
                      (vec2/vec2 (math/rand-between (- hw) hw) hh)

                      3 ;; left
                      (vec2/vec2 (- hw) (math/rand-between (- hh) hh))

                      4 ;; right
                      (vec2/vec2 hw (math/rand-between (- hh) hh)))
          start-dir (-> start-pos
                        (vec2/scale -1)
                        (vec2/unit)
                        (vec2/rotate (rand)))
          ekey (keyword (gensym))
          skey [:enemy ekey]
          enemy-rotate (:enemy-rotate @state/state)
          enemy-bullet-scale (:enemy-bullet-scale @state/state)
          enemy-bullet-gfx (:enemy-bullet-gfx @state/state)
          ]

      (m/with-sprite canvas :enemy
        [enemy (s/make-sprite (:enemy-gfx @state/state) :scale 2)]
        (add! ekey enemy)
        (spatial/add-to-spatial! :default skey (vec2/as-vector start-pos))
        (loop [boid {:mass 10.0 :pos start-pos :vel start-dir
                     :max-force 1.0 :max-speed (:enemy-speed @state/state)}]
          (s/set-pos! enemy (:pos boid))
          (when enemy-rotate
            (s/set-rotation! enemy (+ (vec2/heading (:vel boid)) (/ Math/PI 2))))
          (<! (e/next-frame))

          ;; random shoot? TODO: only shoot when we are pointed
          ;; (somewhat) at the player
          (let [prob (rand)]
            (cond (< prob (:enemy-bullet-probability @state/state))
                  (spawn-bullet! canvas (:pos boid) (:vel boid) (:enemy-bullet-speed @state/state) (:enemy-bullet-life @state/state) enemy-bullet-scale
                                 enemy-bullet-gfx)

                  (< (:enemy-bullet-probability @state/state) prob (+ (:enemy-bullet-probability @state/state) (:enemy-missile-probability @state/state)))
                  (missile/spawn canvas (:pos boid) (:vel boid) (:enemy-missile-life @state/state))))


          ;; check for collision
          (let [matched (->>
                         (spatial/query (:default @spatial/spatial-hashes)
                                        (vec2/as-vector (vec2/sub (:pos boid) (vec2/vec2 32 32)))
                                        (vec2/as-vector (vec2/add (:pos boid) (vec2/vec2 32 32))))
                         keys
                         (filter #(= :bullet (first %)))
                         )]
            (cond
              (pos? (count matched))
              ;; shot!
              (do
                (sound/play-sound :enemy-explode 0.5 false)
                (explosion/explosion canvas enemy true false)
                (bullet/remove! (-> matched first second))
                (spatial/remove-from-spatial :default skey (vec2/as-vector (:pos boid)))
                (remove! ekey)
                (state/add-score! (:enemy-score @state/state))
                (state/shot!)
                )

              (let [pos (:pos boid)
                    ax (Math/abs (vec2/get-x pos))
                    ay (Math/abs (vec2/get-y pos))
                    gutter 64
                    hw (+ gutter (/ (.-innerWidth js/window) 2))
                    hh (+ gutter (/ (.-innerHeight js/window) 2))]
                (or (> ax hw) (> ay hh)))
              ;; off screen
              (do (spatial/remove-from-spatial :default skey (vec2/as-vector (:pos boid)))
                  (remove! ekey))

              :default
              ;; alive!
              (let [next-boid (update-in
                               (if (< (rand) (:enemy-seek-proportion @state/state))
                                 (b/seek boid (vec2/zero))
                                 (b/wander boid
                                           (:enemy-wander-a @state/state)
                                           (:enemy-wander-b @state/state)
                                           (:enemy-wander-c @state/state)
                                           ))
                               [:pos] vec2/sub (:vel @state/state))]
                (spatial/move-in-spatial :default skey
                                         (vec2/as-vector (:pos boid))
                                         (vec2/as-vector (:pos next-boid)))
                (recur next-boid)))))))))
