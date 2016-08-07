(ns cloud-fighter.explosion
  (:require [infinitelives.pixi.sprite :as s]
            [infinitelives.pixi.events :as e]
            [infinitelives.utils.vec2 :as vec2]
            [infinitelives.utils.console :refer [log]]
            [cloud-fighter.state :as state])
  (:require-macros [infinitelives.pixi.macros :as m]
                   [cljs.core.async.macros :refer [go]])
  )

(def explosion-frames [:explode-1 :explode-2
                       :explode-3 :explode-4
                       :explode-5 :explode-6
                       :explode-7])

(def small-explosion-frames [:small-explosion-1
                             :small-explosion-2
                             :small-explosion-3
                             :small-explosion-4
                             :small-explosion-5
                             :small-explosion-6])

(def explosion-speed 6)

(defn explosion [canvas entity move? small?]
  ;(sound/play-sound (keyword (str "explode-" (rand-int 10))) 0.5 false)
  (let [frameset (if small? small-explosion-frames explosion-frames)]
    (go
      (let [initial-pos (s/get-pos entity)
            x (vec2/get-x initial-pos)
            y (vec2/get-y initial-pos)
            frames (count frameset)
            total-frames (* frames explosion-speed)
            ]
        (m/with-sprite canvas :player
          [explosion (s/make-sprite (first frameset)
                                    :scale 4 :x x :y y
                                        ;:rotation (* (rand) Math/PI 2)
                                    )]
          (loop [n 0
                 pos initial-pos]
            (s/set-texture! explosion (get frameset (int (/ n explosion-speed)) (last frameset)))
            (s/set-pos! explosion pos)

            ;; when explosion is maximum size, we can disappear the
            ;; underlying entity
            (when (= 3 n)
              (s/set-visible! entity false))

            (<! (e/next-frame))

            (when (< n total-frames)
              (recur (inc n)
                     (if move?
                       (vec2/sub pos (:vel @state/state))
                       pos)))))))))
