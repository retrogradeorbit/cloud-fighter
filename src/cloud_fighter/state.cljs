(ns cloud-fighter.state
  (:require [infinitelives.utils.vec2 :as vec2]
            [infinitelives.utils.sound :as sound]
            [infinitelives.utils.console :refer [log]]))

(def levels
  [

   ;; biplanes
   {:background 0xa0a0f0
    :max-shot 40
    :num-enemies 4
    :enemy-gfx :biplane
    :enemy-speed 2.0
    :enemy-rotate true
    :enemy-bullet-gfx :bullet
    :enemy-bullet-scale 4
    :enemy-missile-gfx :missile
    :enemy-bullet-speed 2
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.0001
    :enemy-missile-probability 0
    :enemy-missile-life 300
    :enemy-missile-speed 3.3
    :enemy-score 100
    :enemy-seek-proportion 0.3
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.003
    :num-parachutes 1
    :boss :blimp
    :boss-shots 10
    :boss-score 2000
    :boss-speed 1.0
    :boss-missile-probability 0
    :boss-missile-life 300
    :boss-bullet-probability 0
    :boss-bullet-speed 5
    :boss-bullet-life 200}

   {
    :background 0x5d6376
    :max-shot 40
    :num-enemies 6
    :enemy-speed 3
    :enemy-bullet-speed 2
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.0004
    :enemy-missile-speed 3.3
    :enemy-missile-probability 0.0004
    :enemy-missile-life 200
    :enemy-score 100
    :enemy-seek-proportion 0.3
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.01
    :num-parachutes 1
    :boss-shots 10
    :boss-score 2000
    :boss-speed 1.0
    }

   ;; helicopters
   {:background 0xa880cf
    :max-shot 50
    :num-enemies 6
    :enemy-gfx :chopper-1
    :enemy-speed 3.0
    :enemy-bullet-speed 3
    :enemy-bullet-life 200
    :enemy-bullet-probability 0
    :enemy-missile-probability 0.0006
    :enemy-missile-life 300
    :enemy-missile-speed 3.5
    :enemy-score 100
    :enemy-seek-proportion 0.40
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.005
    :num-parachutes 2
    :boss :chinook
    :boss-shots 15
    :boss-score 3000
    :boss-speed 2.0
    :boss-missile-probability 0.001
    :boss-missile-life 300
    :boss-bullet-probability 0
    :boss-bullet-speed 5
    :boss-bullet-life 200
    }

   {
    :background 0x0d7686
    :num-enemies 8
    :enemy-speed 3.2
    :enemy-bullet-speed 3.1
    :enemy-bullet-life 250
    :enemy-bullet-probability 0.0003
    :enemy-missile-probability 0.0006
    :enemy-missile-life 300
    :enemy-missile-speed 3.5
    :enemy-score 100
    :enemy-seek-proportion 0.40
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.03
    :num-parachutes 2
    :boss-shots 10
    :boss-score 3000
    :boss-speed 2.0

    }

   ;; f16s
   {:background 0xa0c8ef
    :max-shot 60
    :num-enemies 8
    :enemy-gfx :f16
    :enemy-speed 3.0
    :enemy-bullet-speed 3
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.0002
    :enemy-missile-probability 0.0004
    :enemy-missile-life 300
    :enemy-missile-speed 3.7
    :enemy-score 100
    :enemy-seek-proportion 0.3
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.01
    :num-parachutes 3
    :boss :b52
    :boss-shots 20
    :boss-score 4000
    :boss-speed 3.0
    :boss-missile-life 300
    :boss-bullet-probability 0.002
    :boss-bullet-speed 3
    :boss-bullet-life 200
    :boss-missile-probability 0.004
    }

   {:background 0x9dba53
    :enemy-speed 3.5
    :enemy-bullet-speed 3.5
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.0004
    :enemy-missile-probability 0.0005
    :enemy-missile-life 300
    :enemy-missile-speed 3.9
    :enemy-score 100
    :enemy-seek-proportion 0.3
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.01
    :num-parachutes 1
    :boss-shots 20
    :boss-score 4000
    :boss-speed 3.0}


   ;; stealth
   {:background 0x7f56ad
    :max-shot 70
    :num-enemies 8
    :enemy-gfx :stealth-fighter
    :enemy-bullet-gfx :stealth-shot
    :enemy-missile-gfx :stealth-missile
    :enemy-speed 3.0
    :enemy-bullet-speed 4
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.0001
    :enemy-missile-probability 0.0004
    :enemy-missile-life 300
    :enemy-missile-speed 3.5
    :enemy-score 100
    :enemy-seek-proportion 0.3
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.01
    :num-parachutes 4
    :boss :stealth-bomber
    :boss-shots 25
    :boss-score 5000
    :boss-speed 4.0
    :boss-missile-probability 0.004
    :boss-missile-life 300
    :boss-bullet-probability 0.004
    :boss-bullet-speed 2
    :boss-missile-speed 3.8
    :boss-bullet-life 200
    }

   {:background 0x496a33
    :enemy-speed 3.5
    :enemy-bullet-speed 3.5
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.0005
    :enemy-missile-probability 0.0005
    :enemy-missile-life 300
    :enemy-missile-speed 3.9
    :enemy-score 100
    :enemy-seek-proportion 0.3
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.01
    :num-parachutes 2
    :boss-shots 25
    :boss-missile-probability 0.006
    :boss-missile-life 300
    :boss-bullet-probability 0.006
    :boss-score 4000
    :boss-speed 4.0}

   ;; ufo
   {:background 0x8476ed
    :enemy-gfx :ufo
    :enemy-rotate false
    :enemy-speed 3.5
    :enemy-bullet-gfx :ufo-shot
    :enemy-bullet-scale 2
    :enemy-missile-gfx :ufo-missile
    :enemy-bullet-speed 3.8
    :enemy-bullet-life 200
    :enemy-bullet-probability 0.0002
    :enemy-missile-probability 0.0002
    :enemy-missile-life 300
    :enemy-missile-speed 3.9
    :enemy-score 100
    :enemy-seek-proportion 0.1
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.01
    :num-parachutes 5
    :boss :mothership
    :boss-shots 30
    :boss-score 10000
    :boss-speed 4.5
    :boss-missile-probability 0.001
    :boss-missile-life 300
    :boss-bullet-probability 0.001
    :boss-bullet-speed 5
    :boss-bullet-life 200

    }


   {:background 0x000000
    :max-shot 30
    :num-enemies 4
    :enemy-speed 3.7
    :enemy-bullet-speed 4
    :enemy-bullet-life 400
    :enemy-bullet-probability 0.0007
    :enemy-missile-probability 0.0005
    :enemy-missile-life 400
    :enemy-missile-speed 3.9
    :enemy-score 100
    :enemy-seek-proportion 0.2
    :enemy-wander-a 8
    :enemy-wander-b 4
    :enemy-wander-c 0.5
    :parachute-prob 0.01
    :num-parachutes 5
    :boss-shots 30
    :boss-score 10000
    :boss-speed 4.5
    :boss-missile-probability 0.002
    :boss-missile-life 300
    :boss-bullet-probability 0.002
    :boss-bullet-speed 3
    :boss-bullet-life 200
    :boss-missile-speed 5}
   ])

(def num-levels (count levels))
(def rotate-speed 0.05)
(def player-speed 4)

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
    :max-shot 2
    :level 0
    :rotate-speed 0.05
    :player-speed 4
    }))

(defn reset-state! []
  (swap! state assoc
         :vel (vec2/vec2 0 -1)
         :alive? true
         :lives 3
         :score 0
         :playing? false
         :shot-count 0
         :max-shot 2
         :level 0
         :rotate-speed 0.05
         :player-speed 4
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
  (swap! state
         (fn [old-state]
           (let [old-score (:score old-state)
                 new-score (+ score old-score)
                 new-life
                 (or
                  ;; extra life at 10000
                  (and (< old-score 10000) (>= new-score 10000))
                  (let [old-adj (- old-score 10000)
                        new-adj (- new-score 10000)
                        ;; and every 50000 after that
                        old-mult (int (/ old-adj 50000))
                        new-mult (int (/ new-adj 50000))]
                    (and (not= old-mult new-mult))))]
             (when new-life (sound/play-sound :one-up 0.5 false))
             (-> old-state
                 (assoc :score new-score)
                 (update-in [:lives] + (if new-life 1 0))

                 ;; no more than 9 lives
                 (update-in [:lives] min 9))))))

(defn load-level [state level]
  (let [cycle (int (/ level num-levels))
        remain (mod level num-levels)
        multi (+ 1 (* 0.25 cycle))]
    (log "level:" level "cycle:" cycle "remain:" remain "multi:" multi)
    (-> state
        (into
         (-> (levels remain)
             (update-in [:enemy-speed] * multi)
             (update-in [:enemy-missile-speed] * multi)
             (update-in [:enemy-bullet-speed] * multi)
             (update-in [:boss-speed] * multi)
             (update-in [:boss-missile-speed] * multi)
             (update-in [:boss-bullet-speed] * multi)
             (assoc :rotate-speed (* rotate-speed multi))
             (assoc :player-speed (* player-speed multi))))
        (assoc :level level)
        (assoc :shot-count 0))))

(defn level-up! []
  (sound/play-sound :level-up 0.5 false)
  (swap! state
         #(let [new-level (inc (:level %))]
            (load-level % new-level))))

(defn level-0! []
  (swap! state load-level 0))
