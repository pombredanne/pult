(ns pult.utils)

(defn current-time []
  (.getTime (js/Date.)))

;;-- Routing helpers
(defn locate!
  "forces browser to change URL;
  It's better than secretary/dispatch; because it makes back button to work;"
  [url]
  (.assign js/document.location (str url)))

;;-- DOM helpers
(defn by-id
  ([id]
    (by-id js/document id))
  ([dom-obj id]
    (.getElementById dom-obj id)))

(defn by-tag-name
  ([dom-obj tag-name]
    (.getElementsByTagName dom-obj tag-name)))

;;TODO: better (hide-by (by-id)...
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


;;-- API helpers
(defn vibrate!
  [duration]
  (.vibrate js/navigator duration))


