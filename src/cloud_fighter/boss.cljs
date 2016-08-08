(ns cloud-fighter.boss
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

(defonce bosses (atom {}))

(defn add! [bkey boss]
  (swap! bosses assoc bkey boss))

(defn remove! [bkey]
  (swap! bosses dissoc bkey))

(defn count-bosses []
  (count @bosses))

(defn constrain [vec w h]
  (let [hw (/ w 2)
        hh (/ h 2)
        x (vec2/get-x vec)
        y (vec2/get-y vec)]
    (vec2/vec2
     (- (mod (+ hw x) w) hw)
     (- (mod (+ hh y) h) hh))))

(defn spawn [canvas]
  (go
    (let [gutter 32
          hw (+ gutter (/ (.-innerWidth js/window) 2))
          hh (+ gutter (/ (.-innerHeight js/window) 2))
          direction (rand-nth [:left :right])
          start-pos (case direction
                      :right
                      (vec2/vec2 (- hw) (math/rand-between (- hh) hh))
                      :left
                      (vec2/vec2 hw (math/rand-between (- hh) hh)))
          dx (case direction :left -1 :right 1)
          bkey (keyword (gensym))
          skey [:boss bkey]]
      (m/with-sprite canvas :enemy
        [boss (s/make-sprite :blimp :scale 3)]
        (s/set-scale! boss (* -3 dx) 3)
        (add! bkey bosses)
        (spatial/add-to-spatial! :default skey (vec2/as-vector start-pos))
        (loop [boid {:mass 10.0 :pos start-pos
                     :vel (vec2/vec2 dx 0)
                     :max-force 1.0 :max-speed 1.0}
               shot-times 0
               ]
          (s/set-pos! boss (:pos boid))
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
              (and (pos? (count matched)) (= shot-times 10))
              ;; final-shot!
              (do
                (bullet/remove! (-> matched first second))
                (spatial/remove-from-spatial :default skey (vec2/as-vector (:pos boid)))
                (remove! bkey))

              ;; key no longer in set
              (not (bkey @bosses))
              (do
                (spatial/remove-from-spatial :default skey (vec2/as-vector (:pos boid)))
                (state/add-score! 10000)
                (score/popup! canvas (:pos boid) 10000 200))

              :default
              ;; alive!
              (let [gutter 128
                    w (+ gutter (.-innerWidth js/window))
                    h (+ gutter (.-innerHeight js/window))
                    hw (/ w 2)
                    hh (/ h 2)
                    next-boid (update-in
                               (b/apply-steering boid (vec2/zero))
                               [:pos]
                               #(-> %
                                    (vec2/sub (:vel @state/state))
                                    (constrain w h)
                                    ))]
                (spatial/move-in-spatial
                 :default skey
                 (vec2/as-vector (:pos boid))
                 (vec2/as-vector (:pos next-boid)))
                (recur next-boid
                       (if (pos? (count matched))
                         ;; shot
                         (do
                           (bullet/remove! (-> matched first second))
                           (inc shot-times))

                         ;; not shot
                         shot-times))))))))))
