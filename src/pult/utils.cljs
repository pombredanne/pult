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
  (let [obj js/navigator
        action-fn (aget js/navigator "vibrate")]
    (log "Here it should vibrate!")
    (try
      ;(.vibrate js/navigator duration); doesnt work event with externs
      (.call action-fn obj duration)
      (catch js/Object e
        (error "Failed to call FirefoxOS api `vibrate`: " e)))))

(defn squuid
  "Constructs a semi-sequential UUID. Useful for creating UUIDs that
  don't fragment indexes. Returns a UUID whose most significant 32
  bits are the current time in milliseconds, rounded to the nearest
  second."
  []
  (let [current-seconds (-> (.now js/Date)
                            (/ 1000)
                            (Math/round))
        seconds-hex (.toString current-seconds 16)
        trailing (.replace "-xxxx-4xxx-yxxx-xxxxxxxxxxxx"
                           (js/RegExp. "[xy]" "g")
                           (fn [c]
                              (let [r (bit-or (* 16 (Math/random)) 0)
                                    v (if (= c "x") r (bit-or (bit-and r 0x3) 0x8))]
                                (.toString v 16))))]
    (UUID. (str seconds-hex trailing))))
