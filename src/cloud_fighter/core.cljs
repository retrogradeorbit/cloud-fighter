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
            [infinitelives.utils.console :refer [log]]

            [cloud-fighter.parallax :as parallax]

            [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]
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
(def scale 4)

(defonce canvas
  (c/init {:layers [:bg :sky :clouds :titles :ui]
           :background sky-colour
           :expand true
           :origins {:roller :left}}))

(defn keyboard-controls [canvas]
  (go
    (m/with-sprite canvas :ui
      [text (pf/make-text :small "Keyboard Controls" :scale font-scale :x 0 :y 150 :tint 0x8080ff :visible false)
       text-2 (pf/make-text :small "←→↑↓" :scale font-scale :x -200 :y 200 :tint 0xffff80 :visible false)
       text-3 (pf/make-text :small "˽ or `z'" :scale font-scale :x -200 :y 230 :tint 0xffff80 :visible false)
       text-4 (pf/make-text :small "Change Ship Direction" :scale font-scale :x 150 :y 200 :tint 0xffff80 :visible false)
       text-5 (pf/make-text :small "Fire Weapon" :scale font-scale :x 150 :y 230 :tint 0xffff80 :visible false)
       ]
      (loop [f 0]
        (when (= f 60) (s/set-visible! text true))
        (when (= f 80) (s/set-visible! text-2 true))
        (when (= f 100) (s/set-visible! text-4 true))
        (when (= f 120) (s/set-visible! text-3 true))
        (when (= f 140) (s/set-visible! text-5 true))
        (<! (e/next-frame))
        (when (< f 350)
          (recur (inc f))))

      (loop [f 0]
        (s/set-x! text-2 (- -200 (Math/pow 1.2 f)))
        (s/set-x! text-3 (- -200 (Math/pow 1.2 f)))
        (s/set-x! text-4 (+ 150 (Math/pow 1.2 f)))
        (s/set-x! text-5 (+ 150 (Math/pow 1.2 f)))

        (<! (e/next-frame))
        (when (< f 40)
          (recur (inc f))))

      (loop [f 0]
        (s/set-y! text (+ 150 (Math/pow 1.2 f)))
        (<! (e/next-frame))
        (when (< f 40)
          (recur (inc f)))))))

(defn gamepad-controls [canvas]
  (go
    (m/with-sprite canvas :ui
      [text (pf/make-text :small "Gamepad Controls" :scale font-scale :x 0 :y 150 :tint 0x8080ff :visible false)
       text-2 (pf/make-text :small "Analog Stick" :scale font-scale :x -200 :y 200 :tint 0xffff80 :visible false)
       text-3 (pf/make-text :small "A, B, C or D" :scale font-scale :x -200 :y 230 :tint 0xffff80 :visible false)
       text-4 (pf/make-text :small "Change Ship Direction" :scale font-scale :x 150 :y 200 :tint 0xffff80 :visible false)
       text-5 (pf/make-text :small "Fire Weapon" :scale font-scale :x 150 :y 230 :tint 0xffff80 :visible false)
       ]
      (loop [f 0]
        (when (= f 60) (s/set-visible! text true))
        (when (= f 80) (s/set-visible! text-2 true))
        (when (= f 100) (s/set-visible! text-4 true))
        (when (= f 120) (s/set-visible! text-3 true))
        (when (= f 140) (s/set-visible! text-5 true))
        (<! (e/next-frame))
        (when (< f 350)
          (recur (inc f))))

      (loop [f 0]
        (s/set-x! text-2 (- -200 (Math/pow 1.2 f)))
        (s/set-x! text-3 (- -200 (Math/pow 1.2 f)))
        (s/set-x! text-4 (+ 150 (Math/pow 1.2 f)))
        (s/set-x! text-5 (+ 150 (Math/pow 1.2 f)))

        (<! (e/next-frame))
        (when (< f 40)
          (recur (inc f))))

      (loop [f 0]
        (s/set-y! text (+ 150 (Math/pow 1.2 f)))
        (<! (e/next-frame))
        (when (< f 40)
          (recur (inc f)))))))

(defn credits [canvas]
  (go
    (m/with-sprite canvas :ui
      [text (pf/make-text :small "Credits" :scale font-scale :x 0 :y 150 :tint 0x8080ff :visible false)
       text-2 (pf/make-text :small "Copyright (C) 2016" :scale font-scale :x -220 :y 200 :tint 0xffff80 :visible false)
       text-3 (pf/make-text :small "Developed in 10 days" :scale font-scale :x -220 :y 230 :tint 0xffff80 :visible false)
       text-4 (pf/make-text :small "By Crispin Wellington" :scale font-scale :x 170 :y 200 :tint 0xffff80 :visible false)
       text-5 (pf/make-text :small "For August 2016 Lisp Gamejam" :scale font-scale :x 170 :y 230 :tint 0xffff80 :visible false)
       ]
      (loop [f 0]
        (when (= f 60) (s/set-visible! text true))
        (when (= f 80) (s/set-visible! text-2 true))
        (when (= f 100) (s/set-visible! text-4 true))
        (when (= f 120) (s/set-visible! text-3 true))
        (when (= f 140) (s/set-visible! text-5 true))
        (<! (e/next-frame))
        (when (< f 350)
          (recur (inc f))))

      (loop [f 0]
        (s/set-x! text-2 (- -220 (Math/pow 1.2 f)))
        (s/set-x! text-3 (- -220 (Math/pow 1.2 f)))
        (s/set-x! text-4 (+ 170 (Math/pow 1.2 f)))
        (s/set-x! text-5 (+ 170 (Math/pow 1.2 f)))

        (<! (e/next-frame))
        (when (< f 40)
          (recur (inc f))))

      (loop [f 0]
        (s/set-y! text (+ 150 (Math/pow 1.2 f)))
        (<! (e/next-frame))
        (when (< f 40)
          (recur (inc f)))))))


(defn titlescreen [canvas]
  (go
    (m/with-sprite canvas :ui
      [fighter-text (s/make-sprite (r/get-texture :fighter-text :nearest) :scale title-scale :x 0 :y 0)
       cloud-text (s/make-sprite (r/get-texture :cloud-text :nearest) :scale title-scale :x 0 :y 0)
       ]
      (go
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

        (parallax/titlescreen-update!)

        (<! (e/next-frame))
        (recur (inc f))
        )))
  )

(defonce main
  (go
    (<! (r/load-resources canvas :ui
                          ["img/clouds/cloud_1.png"
                           "img/clouds/cloud_2.png"
                           "img/clouds/cloud_3.png"
                           "img/clouds/cloud_4.png"
                           "img/clouds/cloud_5.png"
                           "img/clouds/cloud_6.png"
                           "img/clouds/cloud_7.png"
                           "img/clouds/cloud_8.png"
                           "img/clouds/cloud_9.png"
                           "img/clouds/cloud_10.png"
                           "img/clouds/cloud_11.png"
                           "img/clouds/cloud_12.png"
                           "img/clouds/cloud_13.png"
                           "img/clouds/cloud_14.png"
                           "img/clouds/cloud_15.png"
                           "img/clouds/cloud_16.png"
                           "img/clouds/cloud_17.png"
                           "img/clouds/cloud_18.png"
                           "img/clouds/cloud_19.png"
                           "img/clouds/cloud_20.png"
                           "img/clouds/cloud_21.png"
                           "img/clouds/cloud_22.png"
                           "img/clouds/cloud_23.png"
                           "img/clouds/cloud_24.png"
                           "img/cloud-text.png"
                           "img/fighter-text.png"
                           "img/fonts.png"]))

    (pf/pixel-font :small "img/fonts.png" [16 65] [238 111]
                   :chars ["ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                           "abcdefghijklmnopqrstuvwxyz"
                           "0123456789!?#`'.,←→↑↓˽"]
                   :kerning {"fo" -2  "ro" -1 "la" -1 }
                   :space 5)

    (m/with-sprite-set canvas :clouds
      [clouds (parallax/get-sprites)]

      (parallax/cloud-thread clouds)
      (<! (titlescreen canvas)))

    ))
