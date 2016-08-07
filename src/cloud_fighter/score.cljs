(ns cloud-fighter.score
  (:require [infinitelives.utils.vec2 :as vec2]
            [infinitelives.utils.events :as e]
            [infinitelives.utils.boid :as b]
            [infinitelives.pixi.pixelfont :as pf]
            [infinitelives.utils.math :as math]
            [infinitelives.utils.console :refer [log]]
            [infinitelives.utils.spatial :as spatial]
            [infinitelives.pixi.sprite :as s]
            [cloud-fighter.state :as state]
            [cloud-fighter.explosion :as explosion]
            [cloud-fighter.bullet :as bullet]
            [cljs.core.async :refer [<! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [infinitelives.pixi.macros :as m]))

(defn popup! [canvas pos score life]
  (go
    (m/with-sprite canvas :enemy
      [score-text (pf/make-text :small (str score) :scale 2)]
      (loop [life life pos pos]
        (s/set-pos! score-text pos)
        (<! (e/next-frame))
        (when (pos? life)
          (recur (dec life)
                 (vec2/sub pos (:vel @state/state))))))))
