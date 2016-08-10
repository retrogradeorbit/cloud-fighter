(ns cloud-fighter.missile
  (:require [infinitelives.utils.vec2 :as vec2]
            [infinitelives.utils.events :as e]
            [infinitelives.utils.boid :as b]
            [infinitelives.utils.math :as math]
            [infinitelives.utils.console :refer [log]]
            [infinitelives.utils.spatial :as spatial]
            [infinitelives.utils.sound :as sound]
            [infinitelives.pixi.sprite :as s]
            [cloud-fighter.state :as state]
            [cloud-fighter.explosion :as explosion]
            [cloud-fighter.bullet :as bullet]
            [cljs.core.async :refer [<! timeout]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [infinitelives.pixi.macros :as m]))

(def missile-speed 4)

(defonce missiles (atom {}))

(defn add! [mkey missile]
  (swap! missiles assoc mkey missile))

(defn remove! [mkey]
  (swap! missiles dissoc mkey))

(defn count-missiles []
  (count @missiles))

(def pop-frames [:pop-1 :pop-2 :pop-2 :pop-4 :pop-1 :pop-2 :pop-2 :pop-4 ])

(defn pop-anim! [canvas pos]
  (go
    (sound/play-sound :pop 0.5 false)
    (m/with-sprite canvas :enemy
      [pop (s/make-sprite :pop-1 :scale 2)]
      (loop [frame 0 pos pos]
        (s/set-texture! pop (nth pop-frames frame))
        (let [new-pos (loop [n 3 p pos]
                        (s/set-pos! pop p)
                        (<! (e/next-frame))
                        (if (pos? n)
                          (recur (dec n) (vec2/sub p (:vel @state/state)))
                          (vec2/sub p (:vel @state/state))))]
          (when (< frame (count pop-frames))
            (recur (inc frame) new-pos)))))))

(defn spawn [canvas start-pos start-dir life]
  (go
    (sound/play-sound :missile 0.5 false)
    (let [mkey (keyword (gensym))
          skey [:missile mkey]]
      (m/with-sprite canvas :enemy
        [missile (s/make-sprite :missile :scale 2)]
        (add! mkey missile)
        (spatial/add-to-spatial! :default skey (vec2/as-vector start-pos))
        (loop [boid {:mass 10.0 :pos start-pos :vel start-dir
                     :max-force 1.0 :max-speed missile-speed}
               life life]
          (s/set-pos! missile (:pos boid))
          (s/set-rotation! missile (+ (vec2/heading (:vel boid)) (/ Math/PI 2)))
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
                (explosion/explosion canvas missile true true)
                (bullet/remove! (-> matched first second))
                (spatial/remove-from-spatial :default skey (vec2/as-vector (:pos boid)))
                (remove! mkey)
                                        ;(state/add-score! 100)
                )

              (let [pos (:pos boid)
                    ax (Math/abs (vec2/get-x pos))
                    ay (Math/abs (vec2/get-y pos))
                    gutter 64
                    hw (+ gutter (/ (.-innerWidth js/window) 2))
                    hh (+ gutter (/ (.-innerHeight js/window) 2))]
                (or (> ax hw) (> ay hh) (zero? life)))
              ;; off screen or out of life
              (do
                (spatial/remove-from-spatial :default skey (vec2/as-vector (:pos boid)))
                (remove! mkey)

                ;; play pop animation
                (pop-anim! canvas (:pos boid))
                )

              :default
              ;; alive!
              (let [next-boid
                    ;; TODO: use missile turning and constant velocity rather than seek
                    ;; as seek can slow down
                    (update-in
                     (b/seek boid (vec2/zero))
                     [:pos] vec2/sub (:vel @state/state))]
                (spatial/move-in-spatial :default skey
                                         (vec2/as-vector (:pos boid))
                                         (vec2/as-vector (:pos next-boid)))
                (recur next-boid (dec life))))))))))
