(ns cloud-fighter.bullet
  (:require [infinitelives.utils.vec2 :as vec2]
            [infinitelives.pixi.sprite :as s]
            [infinitelives.pixi.events :as e]
            [infinitelives.utils.spatial :as spatial])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [cloud-fighter.async :refer [go-while]]
                   [infinitelives.pixi.macros :as m]
                   [infinitelives.pixi.pixelfont :as pf])
  )

(defonce bullets (atom {}))

(defn remove! [bkey]
  (swap! bullets dissoc bkey))

(defn spawn-bullet! [canvas heading speed lifetime]
  (let [update (-> heading vec2/unit
                   (vec2/scale speed))
        initial-pos (-> heading
                       (vec2/unit)
                       (vec2/scale 40))
        bkey (keyword (gensym))
        skey [:bullet bkey]]
    (go
      (m/with-sprite canvas :player
        [bullet (s/make-sprite :bullet :scale 4 :x 0 :y 0)]
        (swap! bullets assoc bkey bullet)
        (spatial/add-to-spatial! :default skey (vec2/as-vector initial-pos))
        (loop [n lifetime
               pos initial-pos]
          (s/set-pos! bullet pos)
          (<! (e/next-frame))

          (if (and (pos? n) (bkey @bullets))
            (let [next-pos (vec2/add pos update)]
              (spatial/move-in-spatial :default skey
                                       (vec2/as-vector pos)
                                       (vec2/as-vector next-pos))
              (recur (dec n) next-pos))

            (do
              (remove! bkey)
              (spatial/remove-from-spatial :default skey (vec2/as-vector pos)))))))))
