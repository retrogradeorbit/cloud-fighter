(ns cloud-fighter.state
  (:require [infinitelives.utils.vec2 :as vec2]))

(def levels
  [
   {:background 0xa0a0f0
    :max-shot 32
    :num-enemies 4
    :enemy-speed 2.0
    :enemy-bullet-speed 5
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.0001
    :enemy-missile-probability 0
    :enemy-missile-life 300
    :enemy-score 100
    :enemy-seek-proportion 0.3
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :enemy-texture :enemy
    :parachute-prob 0.003
    :num-parachutes 1
    :boss-shots 10
    :boss-score 2000
    :boss-speed 1.0}

   {:background 0xc8a0ef
    :max-shot 48
    :num-enemies 6
    :enemy-speed 3.0
    :enemy-bullet-speed 5
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.0005
    :enemy-missile-probability 0.00005
    :enemy-missile-life 100
    :enemy-score 100
    :enemy-seek-proportion 0.35
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.005
    :num-parachutes 1
    :boss-shots 10
    :boss-score 2000
    :boss-speed 1.0}

   {:background 0xa0c8ef
    :max-shot 4
    :num-enemies 4
    :enemy-speed 3.0
    :enemy-bullet-speed 5
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.001
    :enemy-missile-probability 0.001
    :enemy-missile-life 300
    :enemy-score 100
    :enemy-seek-proportion 0.3
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.01
    :num-parachutes 1
    :boss-shots 10
    :boss-score 2000
    :boss-speed 1.0}

   {:background 0xbf76ed
    :max-shot 5
    :num-enemies 4
    :enemy-speed 3.0
    :enemy-bullet-speed 5
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.001
    :enemy-missile-probability 0.001
    :enemy-missile-life 300
    :enemy-score 100
    :enemy-seek-proportion 0.3
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.01
    :num-parachutes 1
    :boss-shots 10
    :boss-score 2000
    :boss-speed 1.0}

   {:background 0x8476ed
    :max-shot 5
    :num-enemies 4
    :enemy-speed 3.0
    :enemy-bullet-speed 5
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.001
    :enemy-missile-probability 0.001
    :enemy-missile-life 300
    :enemy-score 100
    :enemy-seek-proportion 0.3
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.01
    :num-parachutes 1
    :boss-shots 10
    :boss-score 2000
    :boss-speed 1.0}

   {:background 0xedd376
    :max-shot 5
    :num-enemies 4
    :enemy-speed 3.0
    :enemy-bullet-speed 5
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.001
    :enemy-missile-probability 0.001
    :enemy-missile-life 300
    :enemy-score 100
    :enemy-seek-proportion 0.3
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.01
    :num-parachutes 1
    :boss-shots 10
    :boss-score 2000
    :boss-speed 1.0}

   {:background 0xed7686
    :max-shot 5
    :num-enemies 4
    :enemy-speed 3.0
    :enemy-bullet-speed 5
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.001
    :enemy-missile-probability 0.001
    :enemy-missile-life 300
    :enemy-score 100
    :enemy-seek-proportion 0.3
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.01
    :num-parachutes 1
    :boss-shots 10
    :boss-score 2000
    :boss-speed 1.0}

   {:background 0x9dba53
    :max-shot 5
    :num-enemies 4
    :enemy-speed 3.0
    :enemy-bullet-speed 5
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.001
    :enemy-missile-probability 0.001
    :enemy-missile-life 300
    :enemy-score 100
    :enemy-seek-proportion 0.3
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.01
    :num-parachutes 1
    :boss-shots 10
    :boss-score 2000
    :boss-speed 1.0}

   {:background 0x69ba53
    :max-shot 5
    :num-enemies 4
    :enemy-speed 3.0
    :enemy-bullet-speed 5
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.001
    :enemy-missile-probability 0.001
    :enemy-missile-life 300
    :enemy-score 100
    :enemy-seek-proportion 0.3
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.01
    :num-parachutes 1
    :boss-shots 10
    :boss-score 2000
    :boss-speed 1.0}

   {:background 0xbaa353
    :max-shot 5
    :num-enemies 4
    :enemy-speed 3.0
    :enemy-bullet-speed 5
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.001
    :enemy-missile-probability 0.001
    :enemy-missile-life 300
    :enemy-score 100
    :enemy-seek-proportion 0.3
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.01
    :num-parachutes 1
    :boss-shots 10
    :boss-score 2000
    :boss-speed 1.0}

   {:background 0x000000
    :max-shot 5
    :num-enemies 4
    :enemy-speed 3.0
    :enemy-bullet-speed 5
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.001
    :enemy-missile-probability 0.001
    :enemy-missile-life 300
    :enemy-score 100
    :enemy-seek-proportion 0.3
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.01
    :num-parachutes 1
    :boss-shots 10
    :boss-score 2000
    :boss-speed 1.0}
   ])

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
    :shot-count 0
    :max-shot 8
    }))

(defn reset-state! []
  (swap! state assoc
         :vel (vec2/vec2 0 -1)
         :alive? true
         :lives 3
         :score 0
         :playing? false
         :shot-count 0
         :max-shot 8
         ))

(defn shot! []
  (swap! state
         update-in [:shot-count]
         #(-> % inc (min (:max-shot @state)))))

(defn max-shot-reached? []
  (let [{:keys [max-shot shot-count]} @state]
    (= max-shot shot-count)))

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

(defn load-level [state level]
  (-> state
      (into (levels level))
      (assoc :level level)
      (assoc :shot-count 0)))

(defn level-up! []
  (swap! state
         #(let [new-level (inc (:level %))]
            (load-level % new-level))))

(defn level-0! []
  (swap! state load-level 0))
