(ns cloud-fighter.game
  (:require [infinitelives.pixi.events :as e]
            [infinitelives.pixi.sprite :as s]
            [infinitelives.utils.vec2 :as vec2]
            [infinitelives.utils.events :as events]
            [infinitelives.utils.spatial :as spatial]
            [infinitelives.utils.console :refer [log]]
            [infinitelives.pixi.pixelfont :as pf]
            [cloud-fighter.parallax :as parallax]
            [cloud-fighter.enemy :as enemy]
            [cloud-fighter.state :as state]
            [cloud-fighter.explosion :as explosion]
            [cloud-fighter.bullet :as bullet]
            [infinitelives.utils.gamepad :as gp]
            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [cloud-fighter.async :refer [go-while]]
                   [infinitelives.pixi.macros :as m]
                   [infinitelives.pixi.pixelfont :as pf])
)

(def rotate-speed 0.05)
(def player-speed 4)

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
      (let [turned (vec2/rotate heading rotate-speed)
            dot (side-dot turned dir)]
        ;; if new dot is now negative, weve overturned
        (if (neg? dot) heading turned))

      ;; turn left
      (neg? dot)
      (let [turned (vec2/rotate heading (- rotate-speed))
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

(defn run [canvas player]
  (go
    ;; new spatial hash
    (spatial/new-spatial! :default 64)

    ;; lives icons
    (go
      (m/with-sprite canvas :lives
        [lives (s/make-sprite :player :scale 3 :x 40 :y -40)
         lives-2 (s/make-sprite :player :scale 3 :x (+ 40 60) :y -40)]
        (while true (<! (e/next-frame)))
        ))

    ;; score
    (go
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
            (recur new-score)))
        ))

    ;; loop forever
    (loop [heading (vec2/vec2 0 -1)
           last-fire false]
      (let [fire (fire?)]
        (state/update-pos! (vec2/scale heading player-speed))
        (s/set-rotation! player (vec2/heading (vec2/rotate-90 heading)))

        (when (and fire (not last-fire))
          (bullet/spawn-bullet! canvas heading 10 60))

        (when (or
               (events/is-pressed? :e)
               (gp/button-pressed? 0 :right-bumper))
          (while (events/is-pressed? :e)
            (<! (e/next-frame)))
          (enemy/spawn canvas))

        (when (< (enemy/count-enemies) 8)
          (enemy/spawn canvas))

        (when (events/is-pressed? :s)
          (while (events/is-pressed? :s)
            (<! (e/next-frame)))
          (log (str
                #_ (->>
                 (spatial/query (:default @spatial/spatial-hashes)
                                [-50 -50] [50 50])
                 keys
                 (map first)
                 (into #{}))

                #_ (:hash (:default @spatial/spatial-hashes))

                @bullet/bullets
                ))
          )

        (when (events/is-pressed? :q)
          (while (events/is-pressed? :q)
            (<! (e/next-frame)))
          (explosion/explosion canvas player true))

        ;; check for collision with spatial
        (when
            (and (:alive? @state/state)
                 (:enemy (->>
                          (spatial/query (:default @spatial/spatial-hashes)
                                         [-50 -50] [50 50])
                          keys
                          (map first)
                          (into #{}))))
          (explosion/explosion canvas player false)
          (state/kill-player!)
          )

        (<! (e/next-frame))

        (if (:alive? @state/state)
          ;; alive
          (recur (turned-heading heading) fire)

          ;; dead
          (recur heading true))))))
