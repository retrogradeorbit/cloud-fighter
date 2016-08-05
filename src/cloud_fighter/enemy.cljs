(ns cloud-fighter.enemy
  (:require [infinitelives.utils.vec2 :as vec2]
            [infinitelives.utils.events :as e]
            [infinitelives.utils.boid :as b]
            [infinitelives.utils.spatial :as spatial]
            [infinitelives.pixi.sprite :as s]
            [cloud-fighter.state :as state]
            [cloud-fighter.explosion :as explosion]
            [cloud-fighter.bullet :as bullet]
            [cljs.core.async :refer [<! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [infinitelives.pixi.macros :as m]))

(defonce enemies (atom {}))

(defn add! [ekey enemy]
  (swap! enemies assoc ekey enemy))

(defn remove! [ekey]
  (swap! enemies dissoc ekey))

(defn spawn [canvas]
  (go
    (let [start-pos (-> (vec2/random-unit)
                        (vec2/scale 500)
                                        ;(vec2/add (:pos @state/state))
                        )
          ekey (keyword (gensym))
          skey [:enemy ekey]]
      (m/with-sprite canvas :enemy
        [enemy (s/make-sprite :enemy :scale 2
                                        ;:x (vec2/get-x start-pos)
                                        ;:y (vec2/get-y start-pos)
                              )]
        (add! ekey enemy)
        (spatial/add-to-spatial! :default skey (vec2/as-vector start-pos))
        (loop [boid {:mass 10.0 :pos start-pos :vel (vec2/zero)
                     :max-force 1.0 :max-speed 4.0}]
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
            (if (pos? (count matched))

              ;; shot!
              (do
                (explosion/explosion canvas enemy true)
                (bullet/remove! (-> matched first second))
                (spatial/remove-from-spatial :default skey (vec2/as-vector (:pos boid)))
                (remove! ekey)
                (state/add-score! 100)
                )

              ;; alive!
              (let [next-boid (update-in
                               (if (< (rand) 0.3)
                                 (b/seek boid (vec2/zero))
                                 (b/wander boid 6 3 0.1))
                               [:pos] vec2/sub (:vel @state/state))]
                (spatial/move-in-spatial :default skey
                                         (vec2/as-vector (:pos boid))
                                         (vec2/as-vector (:pos next-boid)))
                (recur next-boid)))))))))
