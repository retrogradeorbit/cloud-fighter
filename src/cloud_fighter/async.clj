(ns cloud-fighter.async
  (:require [clojure.walk :as w]
            [cljs.core.async.macros :as m])
)

;; macros for more convenient game async
(defmacro <!* [test & body]
  `(let [result# (cljs.core.async/<! ~@body)]
     (if ~test
       result#
       (throw (js/Error "go-while exit")))))

(defmacro >!* [test & body]
  `(let [result# (cljs.core.async/>! ~@body)]
     (if ~test
       result#
       (throw (js/Error "go-while exit")))))

(comment
  (macroexpand-1 '(>!* @kill-atom (e/next-frame foo)))
  (macroexpand-1 '(<!* @kill-atom (e/next-frame foo))))

(defn process-body [test [head & tail]]
  (let [
        [processed-head processed-tail]
        (cond
          (identical? '() head) ['() tail]
          (identical? [] head) [[] tail]
          (list? head) [(process-body test head) tail]
          (vector? head) [(vec (process-body test head)) tail]
          (map? head) [(into {} (process-body test (seq head))) tail]
          (= '<! head) ['cloud-fighter.async/<!* (cons test tail)]
          (= '>! head) ['cloud-fighter.async/>!* (cons test tail)]
          :default [head tail])]
    (if processed-tail
      (cons processed-head (process-body test processed-tail))
      (list processed-head))))

(comment
  (process-body 'test2 '(do (foo) (bar) (>! (nf))
                            (foo [])
                            ()
                            {}
                            (loop [t (<! (nf))
                                   p {:a (<! b)
                                      :b :b
                                      (<! c) :c}] (bing) (<! (nf))
                                      (when bong (recur (inc t))))))

  (process-body 'test '(foo [] ())))

(defmacro go-while [test & body]
  `(m/go ~@(process-body test body)))

(comment
  (macroexpand-1
   '(go-while (= a b) (do (foo) (bar) (<! (nf))
                          (loop [] (bing) (<! (nf)) (when bong (recur)))
                          last)
              very-last
              )))
