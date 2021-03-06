(ns cloud-fighter.boss
  (:require [infinitelives.utils.vec2 :as vec2]
            [infinitelives.utils.events :as e]
            [infinitelives.utils.boid :as b]
            [infinitelives.utils.math :as math]
            [infinitelives.utils.console :refer [log]]
            [infinitelives.utils.spatial :as spatial]
            [infinitelives.utils.sound :as sound]
            [infinitelives.pixi.sprite :as s]
            [cloud-fighter.state :as state]
            [cloud-fighter.explosion :as explosion]
            [cloud-fighter.bullet :as bullet]
            [cloud-fighter.score :as score]
            [cloud-fighter.missile :as missile]
            [cljs.core.async :refer [<! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [cloud-fighter.async :refer [go-while]]
                   [infinitelives.pixi.macros :as m]))

(defonce bosses (atom {}))

(defn add! [bkey boss]
  (swap! bosses assoc bkey boss))

(defn remove! [bkey]
  (swap! bosses dissoc bkey))

(defn count-bosses []
  (count @bosses))

(defn constrain [vec w h]
  (let [hw (/ w 2)
        hh (/ h 2)
        x (vec2/get-x vec)
        y (vec2/get-y vec)]
    (vec2/vec2
     (- (mod (+ hw x) w) hw)
     (- (mod (+ hh y) h) hh))))

(defn spawn [canvas]
  (go
    (let [[sfx gain] (sound/play-sound :boss-loop 0.3 true)
          gutter 32
          hw (+ gutter (/ (.-innerWidth js/window) 2))
          hh (+ gutter (/ (.-innerHeight js/window) 2))
          direction (rand-nth [:left :right])
          start-pos (case direction
                      :right
                      (vec2/vec2 (- hw) (math/rand-between (- hh) hh))
                      :left
                      (vec2/vec2 hw (math/rand-between (- hh) hh)))
          dx (case direction :left -1 :right 1)
          bkey (keyword (gensym))
          skey [:boss bkey]
          enemy-bullet-scale (:enemy-bullet-scale @state/state)
          enemy-bullet-gfx (:enemy-bullet-gfx @state/state)
          ]
      (m/with-sprite canvas :enemy
        [boss (s/make-sprite (:boss @state/state) :scale 3)]
        (s/set-scale! boss (* -3 dx) 3)
        (add! bkey bosses)
        (spatial/add-to-spatial! :default skey (vec2/as-vector start-pos))
        (loop [boid {:mass 10.0 :pos start-pos
                     :vel (vec2/vec2 (* (:boss-speed @state/state) dx) 0)
                     :max-force 2.0 :max-speed (:boss-speed @state/state)}
               shot-times 0
               ]
          (s/set-pos! boss (:pos boid))
          (<! (e/next-frame))

          ;; shoot & missile
          (let [prob (rand)]
            (cond (< prob (:boss-bullet-probability @state/state))
                  (cloud-fighter.enemy/spawn-bullet!
                   canvas (:pos boid)
                   (vec2/scale (:pos boid) -1)
                   (:boss-bullet-speed @state/state)
                   (:boss-bullet-life @state/state)
                   enemy-bullet-scale
                   enemy-bullet-gfx)

                  (< (:boss-bullet-probability @state/state) prob (+ (:boss-bullet-probability @state/state) (:boss-missile-probability @state/state)))
                  (missile/spawn canvas (:pos boid) (:vel boid) (:boss-missile-life @state/state))))

          ;; check for collision
          (let [matched (->>
                         (spatial/query (:default @spatial/spatial-hashes)
                                        (vec2/as-vector (vec2/sub (:pos boid) (vec2/vec2 32 32)))
                                        (vec2/as-vector (vec2/add (:pos boid) (vec2/vec2 32 32))))
                         keys
                         (filter #(= :bullet (first %)))
                         )]
            (cond
              (and (pos? (count matched)) (= shot-times (:boss-shots @state/state)))
              ;; final-shot!
              (do
                (.stop sfx)
                (bullet/remove! (-> matched first second))
                (spatial/remove-from-spatial :default skey (vec2/as-vector (:pos boid)))
                (remove! bkey)
                (state/add-score! (:boss-score @state/state))
                (score/popup! canvas (:pos boid) (:boss-score @state/state) 200)
                (state/level-up!)
                (set! (.-backgroundColor (:renderer canvas)) (:background @state/state))
                )

              ;; key no longer in set
              (not (bkey @bosses))
              (do
                (.stop sfx)
                (spatial/remove-from-spatial :default skey (vec2/as-vector (:pos boid)))
                (state/add-score! (:boss-score @state/state))
                (score/popup! canvas (:pos boid) (:boss-score @state/state) 200))

              (not (state/playing?))
              (do
                (.stop sfx)
                (spatial/remove-from-spatial :default skey (vec2/as-vector (:pos boid)))
                (remove! bkey))

              :default
              ;; alive!
              (let [gutter 128
                    w (+ gutter (.-innerWidth js/window))
                    h (+ gutter (.-innerHeight js/window))
                    hw (/ w 2)
                    hh (/ h 2)
                    next-boid (update-in
                               (b/apply-steering boid (vec2/zero))
                               [:pos]
                               #(-> %
                                    (vec2/sub (:vel @state/state))
                                    (constrain w h)
                                    ))]
                (spatial/move-in-spatial
                 :default skey
                 (vec2/as-vector (:pos boid))
                 (vec2/as-vector (:pos next-boid)))
                (recur next-boid
                       (if (pos? (count matched))
                         ;; shot
                         (do
                           (bullet/remove! (-> matched first second))
                           (inc shot-times))

                         ;; not shot
                         shot-times))))))))))
