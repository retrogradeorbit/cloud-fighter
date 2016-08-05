(ns cloud-fighter.state
  (:require [infinitelives.utils.vec2 :as vec2]))

(defonce state
  (atom
   {
    ;; position of player in world
    :pos (vec2/zero)
    :vel (vec2/zero)
    :alive? true
    :lives 3
    :score 0
    }))

(defn update-pos! [vel]
  (swap! state
         #(-> %
              (update-in [:pos] vec2/add vel)
              (assoc :vel vel))))

(defn titlescreen-update! []
  (swap! state
         #(-> %
              (update-in [:pos] vec2/add (vec2/vec2 0 -4))
              (assoc :vel (vec2/vec2 0 -4)))))

(defn kill-player! []
  (swap! state assoc :alive? false))

(defn add-score! [score]
  (swap! state update-in [:score] + score))
