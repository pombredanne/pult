(ns pult.utils)

(defn current-time []
  (.getTime (js/Date.)))

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


