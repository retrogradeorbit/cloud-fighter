(ns cloud-fighter.core
  (:require [devtools.core :as devtools]

            [infinitelives.pixi.canvas :as c]
            [infinitelives.pixi.events :as e]
            [infinitelives.pixi.resources :as r]
            [infinitelives.pixi.texture :as t]
            [infinitelives.pixi.sprite :as s]
            [infinitelives.pixi.pixelfont :as pf]
            [infinitelives.utils.math :as math]
            [infinitelives.utils.sound :as sound]
            [infinitelives.utils.gamepad :as gp]
            [infinitelives.utils.events :as events]
            [infinitelives.utils.console :refer [log]]

            [cloud-fighter.parallax :as parallax]
            [cloud-fighter.game :as game]
            [cloud-fighter.state :as state]

            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [cloud-fighter.async :refer [go-while]]
                   [infinitelives.pixi.macros :as m]
                   [infinitelives.pixi.pixelfont :as pf]
                   ))

(enable-console-print!)
(devtools/install!)

(def sky-colour 0xa0a0f0)
(def title-scale 2)
(def title-ypos -300)
(def title-word-separation 100)
(def font-scale 2)
(def scale 3)

(def spritesheet-assets
  {:player {:pos [0 0] :size [32 32]}
   :bullet {:pos [32 0] :size [16 16]}
   :missile {:pos [32 16] :size [16 16]}
   :enemy {:pos [48 0] :size [32 32]}
   :biplane {:pos [48 0] :size [32 32]}
   :parachute {:pos [80 0] :size [32 32]}
   :blimp {:pos [112 0] :size [48 32]}
   :explode-1 {:pos [0 32] :size [32 32]}
   :explode-2 {:pos [32 32] :size [32 32]}
   :explode-3 {:pos [64 32] :size [32 32]}
   :explode-4 {:pos [96 32] :size [32 32]}
   :explode-5 {:pos [128 32] :size [32 32]}
   :explode-6 {:pos [160 32] :size [32 32]}
   :explode-7 {:pos [192 32] :size [32 32]}
   :pop-1 {:pos [0 64] :size [16 16]}
   :pop-2 {:pos [16 64] :size [16 16]}
   :pop-3 {:pos [32 64] :size [16 16]}
   :pop-4 {:pos [48 64] :size [16 16]}
   :small-explosion-1 {:pos [0 80] :size [16 16]}
   :small-explosion-2 {:pos [16 80] :size [16 16]}
   :small-explosion-3 {:pos [32 80] :size [16 16]}
   :small-explosion-4 {:pos [48 80] :size [16 16]}
   :small-explosion-5 {:pos [60 80] :size [16 16]}
   :small-explosion-6 {:pos [72 80] :size [16 16]}
   :chopper-1 {:pos [0 96] :size [32 32]}
   :chopper-2 {:pos [32 96] :size [32 32]}
   :chinook {:pos [80 96] :size [56 32]}
   :f16 {:pos [0 128] :size [32 32]}
   :stealth-fighter {:pos [0 160] :size [32 32]}
   :stealth-bomber {:pos [48 163] :size [32 57]}
   :ufo {:pos [0 192] :size [32 32]}
   :b52 {:pos [96 160] :size [64 64]}
   :mothership {:pos [176 160] :size [64 32]}
   :ufo-shot {:pos [32 192] :size [16 16]}
   :ufo-missile {:pos [32 208] :size [16 16]}
   :stealth-shot {:pos [32 160] :size [16 16]}
   :stealth-missile {:pos [32 176] :size [16 16]}
})

(defonce canvas
  (c/init {:layers [:clouds-lower :enemy
                    :player :lives :score :count :level :ui]
           :background sky-colour
           :expand true
           :origins {:lives :bottom-left
                     :score :top-left
                     :count :top-right
                     :level :bottom-right}}))

(defn start? []
  (or
   (gp/button-pressed? 0 :a)
   (gp/button-pressed? 0 :b)
   (gp/button-pressed? 0 :x)
   (gp/button-pressed? 0 :y)
   (events/any-pressed?)
   ))

(defn keyboard-controls [canvas]
  (go-while (not (start?))
    (m/with-sprite canvas :ui
      [text (pf/make-text :small "Keyboard Controls" :scale font-scale :x 0 :y 150 :tint 0x8080ff :visible false)
       text-2 (pf/make-text :small "←→↑↓" :scale font-scale :x -200 :y 200 :tint 0xffff80 :visible false)
       text-3 (pf/make-text :small "˽ or `z'" :scale font-scale :x -200 :y 230 :tint 0xffff80 :visible false)
       text-4 (pf/make-text :small "Change Ship Direction" :scale font-scale :x 150 :y 200 :tint 0xffff80 :visible false)
       text-5 (pf/make-text :small "Fire Weapon" :scale font-scale :x 150 :y 230 :tint 0xffff80 :visible false)
       ]
      (loop [f 0]
        (when (= f 60) (sound/play-sound :blip 0.5 false) (s/set-visible! text true))
        (when (= f 80) (sound/play-sound :blip 0.5 false) (s/set-visible! text-2 true))
        (when (= f 100) (sound/play-sound :blip 0.5 false) (s/set-visible! text-4 true))
        (when (= f 120) (sound/play-sound :blip 0.5 false) (s/set-visible! text-3 true))
        (when (= f 140) (sound/play-sound :blip 0.5 false) (s/set-visible! text-5 true))
        (<! (e/next-frame))
        (when (< f 350)
          (recur (inc f))))

      (sound/play-sound :title-slide 0.5 false)
      (loop [f 0]
        (s/set-x! text-2 (- -200 (Math/pow 1.2 f)))
        (s/set-x! text-3 (- -200 (Math/pow 1.2 f)))
        (s/set-x! text-4 (+ 150 (Math/pow 1.2 f)))
        (s/set-x! text-5 (+ 150 (Math/pow 1.2 f)))

        (<! (e/next-frame))
        (when (< f 40)
          (recur (inc f))))

      (sound/play-sound :title-slide 0.5 false)
      (loop [f 0]
        (s/set-y! text (+ 150 (Math/pow 1.2 f)))
        (<! (e/next-frame))
        (when (< f 40)
          (recur (inc f)))))))

(defn gamepad-controls [canvas]
  (go-while (not (start?))
    (m/with-sprite canvas :ui
      [text (pf/make-text :small "Gamepad Controls" :scale font-scale :x 0 :y 150 :tint 0x8080ff :visible false)
       text-2 (pf/make-text :small "Analog Stick" :scale font-scale :x -200 :y 200 :tint 0xffff80 :visible false)
       text-3 (pf/make-text :small "A, B, C or D" :scale font-scale :x -200 :y 230 :tint 0xffff80 :visible false)
       text-4 (pf/make-text :small "Change Ship Direction" :scale font-scale :x 150 :y 200 :tint 0xffff80 :visible false)
       text-5 (pf/make-text :small "Fire Weapon" :scale font-scale :x 150 :y 230 :tint 0xffff80 :visible false)
       ]
      (loop [f 0]
        (when (= f 60) (sound/play-sound :blip 0.5 false) (s/set-visible! text true))
        (when (= f 80) (sound/play-sound :blip 0.5 false) (s/set-visible! text-2 true))
        (when (= f 100) (sound/play-sound :blip 0.5 false) (s/set-visible! text-4 true))
        (when (= f 120) (sound/play-sound :blip 0.5 false) (s/set-visible! text-3 true))
        (when (= f 140) (sound/play-sound :blip 0.5 false) (s/set-visible! text-5 true))
        (<! (e/next-frame))
        (when (< f 350)
          (recur (inc f))))

      (sound/play-sound :title-slide 0.5 false)
      (loop [f 0]
        (s/set-x! text-2 (- -200 (Math/pow 1.2 f)))
        (s/set-x! text-3 (- -200 (Math/pow 1.2 f)))
        (s/set-x! text-4 (+ 150 (Math/pow 1.2 f)))
        (s/set-x! text-5 (+ 150 (Math/pow 1.2 f)))

        (<! (e/next-frame))
        (when (< f 40)
          (recur (inc f))))

      (sound/play-sound :title-slide 0.5 false)
      (loop [f 0]
        (s/set-y! text (+ 150 (Math/pow 1.2 f)))
        (<! (e/next-frame))
        (when (< f 40)
          (recur (inc f)))))))

(defn credits [canvas]
  (go-while (not (start?))
    (m/with-sprite canvas :ui
      [text (pf/make-text :small "Credits" :scale font-scale :x 0 :y 150 :tint 0x8080ff :visible false)
       text-2 (pf/make-text :small "Copyright (C) 2016" :scale font-scale :x -220 :y 200 :tint 0xffff80 :visible false)
       text-3 (pf/make-text :small "Developed in 10 days" :scale font-scale :x -220 :y 230 :tint 0xffff80 :visible false)
       text-4 (pf/make-text :small "By Crispin Wellington" :scale font-scale :x 170 :y 200 :tint 0xffff80 :visible false)
       text-5 (pf/make-text :small "For August 2016 Lisp Gamejam" :scale font-scale :x 170 :y 230 :tint 0xffff80 :visible false)
       ]
      (loop [f 0]
        (when (= f 60) (sound/play-sound :blip 0.5 false) (s/set-visible! text true))
        (when (= f 80) (sound/play-sound :blip 0.5 false) (s/set-visible! text-2 true))
        (when (= f 100) (sound/play-sound :blip 0.5 false) (s/set-visible! text-4 true))
        (when (= f 120) (sound/play-sound :blip 0.5 false) (s/set-visible! text-3 true))
        (when (= f 140) (sound/play-sound :blip 0.5 false) (s/set-visible! text-5 true))
        (<! (e/next-frame))
        (when (< f 350)
          (recur (inc f))))

      (sound/play-sound :title-slide 0.5 false)
      (loop [f 0]
        (s/set-x! text-2 (- -220 (Math/pow 1.2 f)))
        (s/set-x! text-3 (- -220 (Math/pow 1.2 f)))
        (s/set-x! text-4 (+ 170 (Math/pow 1.2 f)))
        (s/set-x! text-5 (+ 170 (Math/pow 1.2 f)))

        (<! (e/next-frame))
        (when (< f 40)
          (recur (inc f))))

      (sound/play-sound :title-slide 0.5 false)
      (loop [f 0]
        (s/set-y! text (+ 150 (Math/pow 1.2 f)))
        (<! (e/next-frame))
        (when (< f 40)
          (recur (inc f)))))))


(defn titlescreen [canvas]
  (go-while (not (start?))
    (m/with-sprite canvas :ui
      [fighter-text (s/make-sprite (r/get-texture :fighter-text :nearest) :scale title-scale :x 0 :y 0)
       cloud-text (s/make-sprite (r/get-texture :cloud-text :nearest) :scale title-scale :x 0 :y 0)

       ]
      (go-while (not (start?))
                (while true
                  (<! (keyboard-controls canvas))
                  (<! (e/wait-frames 60))
                  (<! (gamepad-controls canvas))
                  (<! (e/wait-frames 60))
                  (<! (credits canvas))
                  (<! (e/wait-frames 60)))
                )
      (loop [f 0]

        (s/set-y! cloud-text (+ title-ypos (* 50 (Math/pow (Math/sin (/ f 60)) 2))))
        (s/set-y! fighter-text (+ title-ypos title-word-separation (* 30 (Math/pow (Math/sin (/ f 50)) 2))))

        (s/set-rotation! cloud-text (/ (Math/sin (/ f 60)) 20))
        (s/set-rotation! fighter-text (/ (Math/sin (/ f 50)) -20))

        (s/set-scale! cloud-text (+ title-scale (Math/pow (Math/sin (/ f 60)) 2)))
        (s/set-scale! fighter-text (+ title-scale (Math/pow (Math/sin (/ f 50)) 2)))

        (state/titlescreen-update!)

        (<! (e/next-frame))
        (recur (inc f))
        )))
  )

(defonce main
  (go
    (<! (r/load-resources canvas :ui
                          [
                           "img/cloud_1.png"
                           "img/cloud_2.png"
                           "img/cloud_3.png"
                           "img/cloud_4.png"
                           "img/cloud_5.png"
                           "img/cloud_6.png"
                           "img/cloud_7.png"
                           "img/cloud_8.png"
                           "img/cloud_9.png"
                           "img/cloud_10.png"
                           "img/cloud-text.png"
                           "img/fighter-text.png"
                           "img/fonts.png"
                           "img/sprites.png"
                           "sfx/player-explode.ogg"
                           "sfx/player-shoot.ogg"
                           "sfx/player-start.ogg"
                           "sfx/enemy-explode.ogg"
                           "sfx/parachute-shot.ogg"
                           "sfx/parachute-pickup.ogg"
                           "sfx/missile.ogg"
                           "sfx/pop.ogg"
                           "sfx/one-up.ogg"
                           "sfx/boss-loop.ogg"
                           "sfx/level-up.ogg"
                           "sfx/game-over.ogg"
                           "sfx/enemy-shoot.ogg"
                           "sfx/title-slide.ogg"
                           "sfx/blip.ogg"

]))

    (pf/pixel-font :small "img/fonts.png" [16 65] [250 111]
                   :chars ["ABCDEFGHIJKLMNOPQRSTUVWXYZ/"
                           "abcdefghijklmnopqrstuvwxyz"
                           "0123456789!?#`'.,←→↑↓˽"]
                   :kerning {"fo" -2  "ro" -1 "la" -1 }
                   :space 5)

    (t/load-sprite-sheet! (r/get-texture :sprites :nearest) spritesheet-assets)

    (let [clouds (parallax/get-sprites)]
      (m/with-sprite-set canvas :clouds-lower
        [clouds-lower clouds]
        (m/with-sprite canvas :player
          [player (s/make-sprite :player :scale scale :x 0 :y 0)]

          (parallax/cloud-thread clouds)

          (loop []
            (s/set-visible! player true)
            (s/set-rotation! player 0)

            (state/reset-state!)
            (state/level-0!)
            (set! (.-backgroundColor (:renderer canvas)) (:background @state/state))

            (<! (titlescreen canvas))
            (<! (game/run canvas player))
            (recur)))

          ))


    ))
