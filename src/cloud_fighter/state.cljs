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
    :playing? false
    }))

(defn reset-state! []
  (swap! state assoc
         :vel (vec2/vec2 0 -1)
         :alive? true
         :lives 3
         :score 0
         :playing? false))

(defn playing? []
  (:playing? @state))

(defn play! [play]
  (swap! state assoc :playing? play))

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

(defn alive-player! []
  (swap! state assoc :alive? true))

(defn kill-player! []
  (swap! state assoc :alive? false))

(defn dec-lives! []
  (:lives (swap! state update-in [:lives] dec)))

(defn inc-lives! []
  (:lives (swap! state update-in [:lives]
                 ;; 9 lives is maximum
                 #(-> % dec (max 0) (min 9)))))

(defn get-lives []
  (:lives @state))

(defn add-score! [score]
  (swap! state update-in [:score] + score))
