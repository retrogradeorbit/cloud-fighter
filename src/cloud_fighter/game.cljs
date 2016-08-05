(ns cloud-fighter.game
  (:require [infinitelives.pixi.events :as e]
            [infinitelives.pixi.sprite :as s]
            [infinitelives.utils.vec2 :as vec2]
            [infinitelives.utils.events :as events]
            [infinitelives.utils.spatial :as spatial]
            [infinitelives.utils.console :refer [log]]
            [cloud-fighter.parallax :as parallax]
            [cloud-fighter.enemy :as enemy]
            [cloud-fighter.state :as state]
            [cloud-fighter.explosion :as explosion]
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

(defn spawn-bullet! [canvas heading speed lifetime]
  (let [update (-> heading vec2/unit
                   (vec2/scale speed))
        initial-pos (-> heading
                       (vec2/unit)
                       (vec2/scale 40))
        key [:bullet (keyword (gensym))]]
    (go
      (m/with-sprite canvas :bullets
        [bullet (s/make-sprite :bullet :scale 4 :x 0 :y 0)]
        (spatial/add-to-spatial! :default key (vec2/as-vector initial-pos))
        (loop [n lifetime
               pos initial-pos]
          (s/set-pos! bullet pos)
          (<! (e/next-frame))

          (when (pos? n)
            (let [next-pos (vec2/add pos update)]
              (spatial/move-in-spatial :default key
                                       (vec2/as-vector pos)
                                       (vec2/as-vector next-pos))
              (recur (dec n) next-pos)))

          (spatial/remove-from-spatial :default key (vec2/as-vector pos)))))))

(defn run [canvas player]
  (go
    ;; new spatial hash
    (spatial/new-spatial! :default 64)

    ;; loop forever
    (loop [heading (vec2/vec2 0 -1)
           last-fire false]
      (let [fire (fire?)]
        (state/update-pos! (vec2/scale heading player-speed))
        (s/set-rotation! player (vec2/heading (vec2/rotate-90 heading)))

        (when (and fire (not last-fire))
          (spawn-bullet! canvas heading 10 60))

        (when (events/is-pressed? :e)
          (while (events/is-pressed? :e)
            (<! (e/next-frame)))
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


                (:hash (:default @spatial/spatial-hashes))
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
