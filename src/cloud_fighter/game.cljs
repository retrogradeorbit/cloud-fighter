(ns cloud-fighter.game
  (:require [infinitelives.pixi.events :as e]
            [infinitelives.pixi.sprite :as s]
            [infinitelives.utils.vec2 :as vec2]
            [infinitelives.utils.events :as events]
            [infinitelives.utils.console :refer [log]]
            [cloud-fighter.parallax :as parallax]
            [infinitelives.utils.gamepad :as gp]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [cloud-fighter.async :refer [go-while]]
                   [infinitelives.pixi.macros :as m]
                   [infinitelives.pixi.pixelfont :as pf])
)

(def rotate-speed 0.05)

(defn direction []
  (let [gamepad (vec2/vec2 (or (gp/axis 0) 0)
                           (or (gp/axis 1) 0))]
    (if (> (vec2/magnitude-squared gamepad) 0.8)
      ;; gamepad overrules
      gamepad

      ;; keyboard
      (vec2/vec2
       (cond (events/is-pressed? :left) -1
             (events/is-pressed? :right) 1
             :default 0)
       (cond (events/is-pressed? :up) -1
             (events/is-pressed? :down) 1
             :default 0)))))

(defn turn-dir [heading]
  (let [dir (direction)
        dot (vec2/dot (vec2/rotate-90 heading) dir)]
    (cond
      (and
       (zero? (vec2/get-x dir))
       (zero? (vec2/get-y dir)))
      :none

      (pos? dot)
      :right

      (neg? dot)
      :left

      :default
      :none)))

(defn run [canvas player]
  (go
    ;; loop forever
    (loop [heading (vec2/vec2 0 -1)]
      (parallax/update! (vec2/scale heading 0.5))
      (s/set-rotation! player (vec2/heading (vec2/rotate-90 heading)))
      (<! (e/next-frame))
      (let [turn (turn-dir heading)]
        (recur (vec2/rotate heading
                            (case turn
                              :right rotate-speed
                              :left (- rotate-speed)
                              :none 0)))))))
