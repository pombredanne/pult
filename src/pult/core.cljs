(ns pult.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [chord.client :refer [ws-ch]]
            [cljs.core.async :as async
                             :refer [<! >!]]))

(def app-state (atom {:uri "ws://127.0.0.1:8080/ws"
                      :connection nil
                      :incoming-ch (async/chan 10)
                      :outgoing-ch (async/chan (async/sliding-buffer 10))
                      :event-ch (async/chan (async/sliding-buffer 100))
                      :mappings {"btn-up" :UP
                                 "btn-right" :RIGHT
                                 "btn-left" :LEFT
                                 "btn-down" :DOWN
                                 "btn-a" :X
                                 "btn-b" :Z
                                 "btn-select" :SHIFT
                                 "btn-start" :ENTER}}))

;;-- helpers
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


(defn vibrate!
  [duration]
  (.vibrate js/navigator duration))

(defn read-form-data
  [form-id]
  (let [form-els (by-tag-name (by-id form-id) "input")
        n-els (.-length form-els)]
    (into {}
      (map
        (fn [el] [(.-id el) (.-value el)])
        (for [i (range 0 n-els)]
          (.item form-els i))))))

;;-- message handlers
(defn listen-messages
  [ws-channel incoming-ch]
  (async/pipe ws-channel incoming-ch false))

(defn send-messages
  [ws-channel outgoing-ch]
  (async/pipe outgoing-ch ws-channel false))

;;TODO: handle-received-messages
(defn handle-received-messages
  [incoming-ch]
  (go-loop []
    (when-let [{:keys [message error]} (<! incoming-ch)]
      (if message
        (when (= :command (:id message))
          (.debug js/console "Lag: " (- (current-time) (:start message))))
        (.debug js/console "Got error: " message))
      (vibrate! 20)
      (recur))))

(defn start-messenger
  [url configs]
  (go
    (let [{:keys [ws-channel error]} (<! (ws-ch url configs))
          incoming-ch (:incoming-ch @app-state)
          outgoing-ch (:outgoing-ch @app-state)]
      (if error
        (do
          (.error js/console "Cant open connection with the server." error)
          false)
        (do
          (swap! app-state (fn [xs] (assoc xs :connection ws-channel)))
          (listen-messages ws-channel incoming-ch)
          (send-messages ws-channel outgoing-ch)
          (handle-received-messages incoming-ch)
          (.debug js/console "Connection opened successfully.")
          true)))))

(defn stop-messenger
  []
  (swap! app-state (fn [xs] (assoc xs :connection nil))))

(defn on-disconnect
  [ev]
  (.debug js/console "Closing connection.")
  (stop-messenger)
  (hide-by-id! "controller")
  (show-by-id! "connection"))

(defn on-connect
  [ev]
  (.debug js/console "on connection")
  (let [form-dt (read-form-data "connection-form")
        show-error (fn [id msg]
                     (set! (.-innerHTML (by-id id))
                           (str "<p style = \"background-color: orange;\" >" msg "</p>")))
        uri (str "ws://" (get form-dt "url") ":"
                 (get form-dt "port") "/" (get form-dt "path"))]
    (.debug js/console "Form data:" (pr-str form-dt))
    (.preventDefault ev)
    (go
      (if-let [succ (<! (start-messenger uri {:format :edn}))]
        (do
          (.debug js/console "Connection success.")
          (swap! app-state (fn [xs] (assoc xs :uri uri)))
          (hide-by-id! "connection")
          (show-by-id! "controller"))
        (do
          (.debug js/console "Connection failure.")
          (show-error "connection-msg" "Connection failure!")
          )))))

(defn register-events!
  [doc-obj selector-actions]
  (doseq [[selector action-name action-fn] selector-actions]
    (if-let [target (by-id doc-obj selector)]
      (.addEventListener target action-name action-fn)
      (.error js/console
              (str "Failed to register event for:" selector " , " action-name)))))

(defn ^:export main
  []
  (let [control-obj (by-id js/document "control-object")
        svg-doc (.-contentDocument control-obj)
        incoming-ch (:incoming-ch @app-state) ;(async/chan 10)
        outgoing-ch (:outgoing-ch @app-state) ;(async/chan (async/sliding-buffer 10))
        on-action (fn [ev]
                    (.debug js/console "user clicked on action button" ev)
                    (let [btn-id (.. ev -target -id)]
                      ;;TODO: keep flooding until keyUP
                      (async/put! outgoing-ch
                                  {:id :command
                                   :action :key-press
                                   :duration 2
                                   :delay 5
                                   :key (get-in @app-state [:mappings btn-id] :not-mapped)
                                   :times 1
                                   :start (current-time)})
                      (vibrate! 10)))]
    (if (nil? svg-doc)
      (.error js/console "Failed to load controller UI.")
      (do
        (.debug js/console "Registering button actions...")
        ;;-- register app events
        (register-events! js/document
                          [["connection-submit" "click" on-connect]])
        ;;-- register controller events
        (register-events! svg-doc
                          [["btn-up"    "click" on-action]
                           ["btn-down"  "click" on-action]
                           ["btn-left"  "click" on-action]
                           ["btn-right" "click" on-action]
                           ["btn-a"     "click" on-action]
                           ["btn-b"     "click" on-action]
                           ["btn-select" "click" on-action]
                           ["btn-start" "click" on-action]
                           ["btn-configure" "click" on-action]
                           ["btn-connect" "click" on-disconnect]
                           ])))))

(main)
