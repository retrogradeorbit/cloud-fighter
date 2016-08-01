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
(def title-ypos -250)
(def scale 4)

(defonce canvas
  (c/init {:layers [:bg :sky :clouds :titles :ui]
           :background sky-colour
           :expand true
           :origins {:roller :left}}))

(defn titlescreen [canvas]
  (go
    (m/with-sprite canvas :ui
      [fighter-text (s/make-sprite (r/get-texture :fighter-text :nearest) :scale title-scale :x 0 :y 0)
       cloud-text (s/make-sprite (r/get-texture :cloud-text :nearest) :scale title-scale :x 0 :y 0)
       ]
      (loop [f 0]

        (s/set-y! cloud-text (+ title-ypos (* 50 (Math/pow (Math/sin (/ f 60)) 2))))
        (s/set-y! fighter-text (+ title-ypos 80 (* 30 (Math/pow (Math/sin (/ f 50)) 2))))

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
                           "img/fighter-text.png"]))

    (m/with-sprite-set canvas :clouds
      [clouds (parallax/get-sprites)]

      (parallax/cloud-thread clouds)
      (<! (titlescreen canvas)))

    ))
