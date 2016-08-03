(ns cloud-fighter.enemy
  (:require [infinitelives.utils.vec2 :as vec2]
            [infinitelives.utils.events :as e]
            [infinitelives.utils.boid :as b]
            [infinitelives.pixi.sprite :as s]
            [cljs.core.async :refer [<! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [infinitelives.pixi.macros :as m]))

(defn spawn [canvas]
  (go
    (let [start-pos (-> (vec2/random-unit)
                        (vec2/scale 200))]
      (m/with-sprite canvas :player
        [enemy (s/make-sprite :enemy :scale 2
                              :x (vec2/get-x start-pos)
                              :y (vec2/get-y start-pos))]
        (loop [boid {:mass 10.0 :pos start-pos :vel (vec2/zero)
                     :max-force 1.0 :max-speed 4.0}]
          (<! (e/next-frame))

          (s/set-pos! enemy (:pos boid))
          (s/set-rotation! enemy (+ (vec2/heading (:vel boid)) (/ Math/PI 2)))

          (recur
           (if (< (rand) 0.3)
             (b/seek boid (vec2/zero))
             (b/wander boid 6 3 0.1))
           ))))))
