(ns cloud-fighter.parachute
  (:require [infinitelives.utils.vec2 :as vec2]
            [infinitelives.utils.events :as e]
            [infinitelives.utils.boid :as b]
            [infinitelives.utils.math :as math]
            [infinitelives.utils.console :refer [log]]
            [infinitelives.utils.spatial :as spatial]
            [infinitelives.pixi.sprite :as s]
            [cloud-fighter.state :as state]
            [cloud-fighter.explosion :as explosion]
            [cloud-fighter.bullet :as bullet]
            [cloud-fighter.score :as score]
            [cloud-fighter.missile :as missile]
            [cljs.core.async :refer [<! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [infinitelives.pixi.macros :as m]))

(defonce parachutes (atom {}))

(defn add! [pkey parachute]
  (swap! parachutes assoc pkey parachute))

(defn remove! [pkey]
  (swap! parachutes dissoc pkey))

(defn count-parachutes []
  (count @parachutes))

(defn spawn [canvas]
  (go
    (let [gutter 32
          hw (+ gutter (/ (.-innerWidth js/window) 2))
          hh (+ gutter (/ (.-innerHeight js/window) 2))
          start-pos (case (math/rand-between 1 4)
                      1 ;; top
                      (vec2/vec2 (math/rand-between (- hw) hw) (- hh))

                      2 ;; bottom
                      (vec2/vec2 (math/rand-between (- hw) hw) hh)

                      3 ;; left
                      (vec2/vec2 (- hw) (math/rand-between (- hh) hh))

                      4 ;; right
                      (vec2/vec2 hw (math/rand-between (- hh) hh)))
          pkey (keyword (gensym))
          skey [:parachute pkey]]
      (m/with-sprite canvas :enemy
        [parachute (s/make-sprite :parachute :scale 3)]
        (add! pkey parachutes)
        (spatial/add-to-spatial! :default skey (vec2/as-vector start-pos))
        (loop [boid {:mass 10.0 :pos start-pos :vel (vec2/zero)
                     :max-force 1.0 :max-speed 1.0}
               fnum 0
               ]
          (s/set-pos! parachute (:pos boid))
          (s/set-rotation! parachute (/ (Math/sin (/ fnum 30)) 2))
          (<! (e/next-frame))

          ;; check for collision
          (let [matched (->>
                         (spatial/query (:default @spatial/spatial-hashes)
                                        (vec2/as-vector (vec2/sub (:pos boid) (vec2/vec2 32 32)))
                                        (vec2/as-vector (vec2/add (:pos boid) (vec2/vec2 32 32))))
                         keys
                         (filter #(= :bullet (first %)))
                         )]
            (cond
              (pos? (count matched))
              ;; shot!
              (do
                (bullet/remove! (-> matched first second))
                (spatial/remove-from-spatial :default skey (vec2/as-vector (:pos boid)))
                (remove! pkey))

              (let [pos (:pos boid)
                    ax (Math/abs (vec2/get-x pos))
                    ay (Math/abs (vec2/get-y pos))
                    gutter 64
                    hw (+ gutter (/ (.-innerWidth js/window) 2))
                    hh (+ gutter (/ (.-innerHeight js/window) 2))]
                (or (> ax hw) (> ay hh)))
              ;; off screen
              (do (spatial/remove-from-spatial :default skey (vec2/as-vector (:pos boid)))
                  (remove! pkey))

              ;; key no longer in set
              (not (pkey @parachutes))
              (do
                (spatial/remove-from-spatial :default skey (vec2/as-vector (:pos boid)))
                (state/add-score! 2000)
                (score/popup! canvas (:pos boid) 2000 200))

              :default
              ;; alive!
              (let [next-boid (update-in
                               (b/wander boid 8 4 0.5)
                               [:pos] vec2/sub (:vel @state/state))]
                (spatial/move-in-spatial
                 :default skey
                 (vec2/as-vector (:pos boid))
                 (vec2/as-vector (:pos next-boid)))
                (recur next-boid (inc fnum))))))))))
