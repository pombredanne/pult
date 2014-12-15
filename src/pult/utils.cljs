(ns pult.utils)

(defn current-time []
  (.getTime (js/Date.)))

;;-- DOM helpers
(defn by-id
  ([id]
    (by-id js/document id))
  ([dom-obj id]
    (.getElementById dom-obj id)))

(defn by-tag-name
  ([dom-obj tag-name]
    (.getElementsByTagName dom-obj tag-name)))

(defn hide-by-id!
  [id]
  (.setAttribute (by-id id) "style" "display:none"))

(defn show-by-id!
  [id]
  (.setAttribute (by-id id) "style" "display:block"))

;;-- logging helpers
(defn log [& msgs]
  (.log js/console (apply str msgs)))

(defn error [& msgs]
  (.error js/console (apply str msgs)))

