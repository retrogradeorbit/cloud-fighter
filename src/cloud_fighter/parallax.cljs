(ns cloud-fighter.parallax
  (:require
   [infinitelives.utils.math :as math]
   [infinitelives.utils.vec2 :as vec2]
   [infinitelives.utils.console :refer [log]]
   [infinitelives.pixi.events :as e]
   [infinitelives.pixi.sprite :as s]
   [infinitelives.pixi.resources :as r]
   [cloud-fighter.state :as state]
   [cljs.core.async :refer [<!]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [infinitelives.pixi.macros :as m])
)


(def num-clouds 20)
(def cloud-choice [:cloud_1 :cloud_2 :cloud_3 :cloud_4 :cloud_5 :cloud_6 :cloud_7 :cloud_8 :cloud_9 :cloud_10])
(def cloud-set
  (sort-by :z
           (map (fn [n]
                  (let [depth (math/rand-between 0 (dec (count cloud-choice)))]
                    {:x (math/rand-between 0 2048)
                     :y (math/rand-between 0 2048)
                     :z (+ 2 depth (rand))
                     :depth depth})) (range num-clouds))))

(def scale 3)
(def edge-gutter (* 128 scale))

(defn get-sprites []
  (for [{:keys [x y z depth]} cloud-set]
    (s/make-sprite
     (r/get-texture (rand-nth cloud-choice) :nearest)
     :x (* scale x)
     :y (* scale y)
     :scale scale
     ;;:alpha 0.5
     )))

(defn set-cloud-positions! [clouds [xp yp]]
  (let [w (+ edge-gutter (.-innerWidth js/window))
        h (+ edge-gutter (.-innerHeight js/window))
        hw (/ w 2)
        hh (/ h 2)]
    (doall
     (map
      (fn [{:keys [x y z] :as old} sprite]
        (s/set-pos! sprite
                    (- (mod (+ (* 4 x) (* 0.1 (- xp) z)) w) hw)
                    (- (mod (+ (* 4 y) (* 0.1 (- yp) z)) h) hh)))
      cloud-set
      clouds))))


(defn cloud-thread [clouds]
  (go
    (loop [c 0]
      (<! (e/next-frame))
      (set-cloud-positions! clouds (vec2/as-vector (:pos @state/state)))
      (recur (inc c)))))
