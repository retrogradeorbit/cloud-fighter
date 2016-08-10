(ns cloud-fighter.game
  (:require [infinitelives.pixi.events :as e]
            [infinitelives.pixi.sprite :as s]
            [infinitelives.utils.vec2 :as vec2]
            [infinitelives.utils.events :as events]
            [infinitelives.utils.spatial :as spatial]
            [infinitelives.utils.sound :as sound]
            [infinitelives.utils.console :refer [log]]
            [infinitelives.pixi.pixelfont :as pf]
            [cloud-fighter.parallax :as parallax]
            [cloud-fighter.enemy :as enemy]
            [cloud-fighter.state :as state]
            [cloud-fighter.explosion :as explosion]
            [cloud-fighter.bullet :as bullet]
            [cloud-fighter.score :as score]
            [cloud-fighter.parachute :as parachute]
            [cloud-fighter.boss :as boss]
            [infinitelives.utils.gamepad :as gp]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [cloud-fighter.async :refer [go-while]]
                   [infinitelives.pixi.macros :as m]
                   [infinitelives.pixi.pixelfont :as pf])
)

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

(defn fire? []
  (or
   (events/is-pressed? :z)
   (events/is-pressed? :space)
   (gp/button-pressed? 0 :a)
   (gp/button-pressed? 0 :b)
   (gp/button-pressed? 0 :x)
   (gp/button-pressed? 0 :y)))

(defn side-dot [heading dir]
  (vec2/dot (vec2/rotate-90 heading) dir))

(defn turned-heading [heading]
  (let [dir (direction)
        dot (side-dot heading dir)]
    (cond
      (and
       (zero? (vec2/get-x dir))
       (zero? (vec2/get-y dir)))
      heading

      ;; turn right
      (pos? dot)
      (let [turned (vec2/rotate heading (:rotate-speed @state/state))
            dot (side-dot turned dir)]
        ;; if new dot is now negative, weve overturned
        (if (neg? dot) heading turned))

      ;; turn left
      (neg? dot)
      (let [turned (vec2/rotate heading (- (:rotate-speed @state/state)))
            dot (side-dot turned dir)]
        ;; if new dot is now posative, weve overturned
        (if (pos? dot) heading turned))

      :default
      heading)))

(defn change-text! [batch font-key text]
  (let [font (pf/get-font font-key)]
    (loop [[c & l] (seq text)
           xp 0 yp 0
           last-c nil]
      (let [char ((:font font) c)
            {:keys [texture pos size]} char
            [x y] pos
            [w h] size
            pair (str last-c c)
            koff ((:kerning font) pair)
            ]
        (if (nil? char)
          ;; if character is not present in font map, put a space
          (when (seq l)
            (recur l (+ xp (:space font)) yp c))

          (do
            ;character is present, add the sprite to the container
            (.addChild batch (s/make-sprite texture :x (+ xp koff) :y yp :xhandle 0 :yhandle 0))
            (if (seq l)
              (recur l (+ xp w 1.0 koff) yp c)
              (s/set-pivot! batch (/ (+ xp w koff) 2.0) 0))))))))

(defn get-ready [canvas heading]
  (go
    (sound/play-sound :player-start 0.5 false)
    (m/with-sprite canvas :ui
      [player-one-text (pf/make-text :small "Player One" :scale 3 :x 0 :y -116
                                     :tint 0xff4080)
       get-ready-text (pf/make-text :small "Get Ready" :scale 3 :x 0 :y 100
                                    :tint 0xff4080)]
      (loop [f 200]
        (state/update-pos! (vec2/scale heading (:player-speed @state/state)))
        (<! (e/next-frame))
        (when (pos? f)
          (recur (dec f)))))))

(defn game-over [canvas heading]
  (go
    (sound/play-sound :game-over 0.5 false)
    (m/with-sprite canvas :ui
      [player-one-text (pf/make-text :small "Game Over" :scale 3 :x 0 :y 0
                                     :tint 0xff4080)]
      (loop [f 200]
        (state/update-pos! (vec2/scale heading (:player-speed @state/state)))
        (<! (e/next-frame))
        (when (pos? f)
          (recur (dec f)))))))

(defn update-lives-icons! [lives-set]
  (doseq [n (range (state/get-lives))] (s/set-visible! (nth lives-set n) true))
  (doseq [n (range (state/get-lives) 9)] (s/set-visible! (nth lives-set n) false)))

(defn lives-icon-display [canvas]
  (go-while (state/playing?)
      (m/with-sprite-set canvas :lives
        [lives-set (for [n (range 9)] (s/make-sprite :player :scale 3 :x (+ 40 (* 60 n)) :y -40))]
        (update-lives-icons! lives-set)
        (while true
          (<! (e/next-frame))
          (update-lives-icons! lives-set)))))

(defn score-display [canvas]
  (go-while (state/playing?)
    (m/with-sprite canvas :score
      [score-text (pf/make-text :small (-> @state/state :score str)
                                :scale 3
                                :x 100 :y 16)]
      (loop [score (:score @state/state)]
        (<! (e/next-frame))
        (let [new-score (:score @state/state)]
          (when (not= new-score score)
            (.removeChildren score-text)
            (change-text! score-text :small (str new-score)))
          (recur new-score))))))

(defn level-display [canvas]
  (go-while (state/playing?)
    (m/with-sprite canvas :level
      [level-text (pf/make-text :small (->> @state/state :level inc str)
                                :scale 3
                                :x -32 :y -48)]
      (loop [level (inc (:level @state/state))]
        (<! (e/next-frame))
        (let [new-level (inc (:level @state/state))]
          (when (not= new-level level)
            (.removeChildren level-text)
            (change-text! level-text :small (str new-level)))
          (recur new-level))))))

(defn get-shot-string []
  (str (:shot-count @state/state) "/" (:max-shot @state/state)))

(defn shot-count-display [canvas]
  (go-while (state/playing?)
            (m/with-sprite canvas :count
              [shot-count-text
               (pf/make-text
                :small (get-shot-string)
                :scale 3 :x -100 :y 16)]
              (loop [shot-count (:shot-count @state/state)]
                (<! (e/next-frame))
                (let [new-shot-count (:shot-count @state/state)]
                  (when (not= new-shot-count shot-count)
                    (.removeChildren shot-count-text)
                    (change-text!
                     shot-count-text
                     :small (get-shot-string)))
                  (recur new-shot-count))))))

(defn run [canvas player]
  (go
    ;; new spatial hash
    (spatial/new-spatial! :default 64)
    (state/level-0!)
    (set! (.-backgroundColor (:renderer canvas)) (:background @state/state))
    (state/play! true)
    (lives-icon-display canvas)
    (score-display canvas)
    (level-display canvas)
    (shot-count-display canvas)

    ;; loop forever
    (loop [heading (vec2/vec2 0 -1)
           last-fire false]
      (let [fire (fire?)]
        (state/update-pos! (vec2/scale heading (:player-speed @state/state)))
        (s/set-rotation! player (vec2/heading (vec2/rotate-90 heading)))

        (when (and fire (not last-fire))
          (sound/play-sound :player-shoot 0.5 false)
          (bullet/spawn-bullet! canvas heading 10 60))

        (when (and (:alive? @state/state) (< (enemy/count-enemies)
                                             (:num-enemies @state/state)))
          (enemy/spawn canvas))

        (when (and (< (parachute/count-parachutes) (:num-parachutes @state/state))
                   (< (rand) (:parachute-prob @state/state)))
          (parachute/spawn canvas))

        (when (and (zero? (boss/count-bosses)) (state/max-shot-reached?))
          (boss/spawn canvas))

        ;; check for collision with spatial
        (let [collided-objs (->>
                            (spatial/query (:default @spatial/spatial-hashes)
                                           [-50 -50] [50 50])
                            keys)
              collided-set (->> collided-objs
                                (map first)
                                (into #{}))]
          (when
              (and (:alive? @state/state)
                   (or (:enemy collided-set) (:enemy-bullet collided-set)
                       (:boss collided-set) (:missile collided-set)))

            ;; TODO: remove enemy-bullet when collided with bullet
            (explosion/explosion canvas player false false)
            (sound/play-sound :player-explode 0.5 false)
            (state/kill-player!))

          (when (:parachute collided-set)
            (let [pkey (->> collided-objs
                            (group-by first)
                            :parachute
                            first
                            second)]
              ;; removing the key triggers the score and parachute disappear
              (parachute/remove! pkey))))

        (<! (e/next-frame))

        (if (:alive? @state/state)
          ;; alive
          (recur (turned-heading heading) fire)

          ;; dead
          (do
            (loop [f 200]
              (state/update-pos! (vec2/scale heading (:player-speed @state/state)))
              (<! (e/next-frame))
              (when (pos? f) (recur (dec f))))

            (if (zero? (state/get-lives))
              ;; game over
              (do
                (<! (game-over canvas heading))
                (state/play! false))

              ;; next life
              (let [new-lives (state/dec-lives!)]
                (state/alive-player!)
                (s/set-visible! player true)
                (<! (get-ready canvas heading))
                (recur heading true)))))))))
