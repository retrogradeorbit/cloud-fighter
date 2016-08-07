(ns cloud-fighter.enemy
  (:require [infinitelives.utils.vec2 :as vec2]
            [infinitelives.utils.events :as e]
            [infinitelives.utils.boid :as b]
            [infinitelives.utils.math :as math]
            [infinitelives.utils.spatial :as spatial]
            [infinitelives.pixi.sprite :as s]
            [cloud-fighter.state :as state]
            [cloud-fighter.explosion :as explosion]
            [cloud-fighter.bullet :as bullet]
            [cloud-fighter.missile :as missile]
            [cljs.core.async :refer [<! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [infinitelives.pixi.macros :as m]))

(def bullet-speed 5)
(def bullet-life 200)
(def bullet-probability 0.001)
(def missile-probability 0.001)
(def missile-life 300)

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

(defn spawn-bullet! [canvas initial-pos heading speed lifetime]
  (let [update (-> heading vec2/unit
                   (vec2/scale speed))
        bkey (keyword (gensym))
        skey [:enemy-bullet bkey]]
    (go
      (m/with-sprite canvas :bullets
        [bullet (s/make-sprite :bullet :scale 4 :x 0 :y 0)]
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
          skey [:enemy ekey]]
      (m/with-sprite canvas :enemy
        [enemy (s/make-sprite :enemy :scale 2
                                        ;:x (vec2/get-x start-pos)
                                        ;:y (vec2/get-y start-pos)
                              )]
        (add! ekey enemy)
        (spatial/add-to-spatial! :default skey (vec2/as-vector start-pos))
        (loop [boid {:mass 10.0 :pos start-pos :vel start-dir
                     :max-force 1.0 :max-speed 3.0}]
          (s/set-pos! enemy (:pos boid))
          (s/set-rotation! enemy (+ (vec2/heading (:vel boid)) (/ Math/PI 2)))
          (<! (e/next-frame))



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
                (explosion/explosion canvas enemy true)
                (bullet/remove! (-> matched first second))
                (spatial/remove-from-spatial :default skey (vec2/as-vector (:pos boid)))
                (remove! ekey)
                (state/add-score! 100)
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
                               (if (< (rand) 0.3)
                                 (b/seek boid (vec2/zero))
                                 (b/wander boid 8 4 0.5))
                               [:pos] vec2/sub (:vel @state/state))]
                (spatial/move-in-spatial :default skey
                                         (vec2/as-vector (:pos boid))
                                         (vec2/as-vector (:pos next-boid)))
                (recur next-boid)))))))))
