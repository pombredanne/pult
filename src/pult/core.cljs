(ns pult.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [chord.client :refer [ws-ch]]
            [cljs.core.async :as async
                             :refer [<! >!]]
            [freactive.dom :as dom]
            [pult.views.connection :as conn-app]))

(defonce app-state (atom {:uri "ws://127.0.0.1:8080/ws"
                          :connection nil
                          :incoming-ch (async/chan 5)
                          :outgoing-ch (async/chan (async/sliding-buffer 5))
                          :event-ch (async/chan 10)
                          :freq 10 ;how often generate impulses
                          :mappings {"btn-up" :UP
                                     "btn-right" :RIGHT
                                     "btn-left" :LEFT
                                     "btn-down" :DOWN
                                     "btn-a" :A
                                     "btn-b" :S
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

(defn connect
  [conn-dt]
  (.debug js/console "on connection")
  (let [{:keys [url port path]} (:data conn-dt)
        show-error (fn [id msg]
                     (set! (.-innerHTML (by-id id))
                           (str "<p style = \"background-color: orange;\" >" msg "</p>")))
        uri (str "ws://" url ":" port "/" path)]
    (vibrate! 20)
    (go
      (if-let [succ (<! (start-messenger uri {:format :edn}))]
        (do
          (.debug js/console "Connection success.")
          (swap! app-state (fn [xs] (assoc xs :uri uri)))
          (hide-by-id! "connection")
          (show-by-id! "controller"))
        (do
          (.debug js/console "Connection failure.")
          (show-error "connection-msg" "Connection failure!"))))))

(defn on-connect
  [event-feed source-id]
  (let [data-ch (async/chan 1)]
    (.debug js/console "Waiting connection data.")
    (async/sub event-feed source-id data-ch)
    (go-loop []
      (when-let [conn-dt (<! data-ch)]
        (connect conn-dt)
        (recur)))))

(defn register-events!
  [doc-obj selector-actions]
  (doseq [[selector action-name action-fn] selector-actions]
    (if-let [target (by-id doc-obj selector)]
      (.addEventListener target action-name action-fn)
      (.error js/console
              (str "Failed to register event for:" selector " , " action-name)))))

(defmulti event->action
  (fn [ev] (.-type ev)))

(defmethod event->action "click" [ev]
  {:action :key-press
   :duration 30
   :release? true})

(defmethod event->action "touchstart" [ev]
  {:action :key-press
   :release? false})

(defmethod event->action "touchend" [ev]
  {:action :key-release})

(defmethod event->action "mousedown" [ev]
  {:action :key-press
   :release? false})

(defmethod event->action "mouseup" [ev]
  {:action :key-release})

(defmethod event->action :default [ev]
  (.error js/console "Unknown event - cant translate it to action." ev))

(defn send-actions
  "listen controller actions on event channel and sends it to the server"
  [outgoing-ch event-feed source-id]
  (let [ctrl-ch (async/chan (async/sliding-buffer 5))]
    (async/sub event-feed source-id ctrl-ch)
    (.debug js/console "Listening controller events ...")
    (go-loop []
      (when-let [dt (<! ctrl-ch)]
        (let [duration (:duration @app-state)
              btn-id (.. (:event dt) -target -id)
              btn-code (get-in @app-state [:mappings btn-id] :not-mapped)]
          (.debug js/console (:event dt))
          (some->>
                (:event dt)
                event->action
                (merge {:id :command
                        :key btn-code
                        :start (current-time)})
                (async/put! outgoing-ch))
          (vibrate! duration)
          (recur))))))

(defn ^:export main []
  (let [connection-container (by-id "connection")
        control-obj (by-id js/document "control-object")
        svg-doc (.-contentDocument control-obj)
        incoming-ch (:incoming-ch @app-state)
        outgoing-ch (:outgoing-ch @app-state)
        event-ch (:event-ch @app-state)
        event-feed (async/pub event-ch :source)
        into-feed (fn [ev]
                   (async/put! event-ch
                               {:source :controller
                                :event ev}))]
    (if (nil? svg-doc)
      (.error js/console "Failed to load controller UI.")
      (do
        (.debug js/console "Registering components...")
        ;;-- init app views
        (dom/mount! connection-container (conn-app/main app-state))

        ;;TODO: into own component-view;
        (.debug js/console "Registering button actions...")
        ;;-- register controller events
        (register-events! svg-doc
                          [
                           ["btn-up"    "touchstart"  into-feed]
                           ["btn-up"    "touchend"    into-feed]
                           ["btn-up"    "mousedown"   into-feed]
                           ["btn-up"    "mouseup"     into-feed]

                           ["btn-down"  "touchstart"  into-feed]
                           ["btn-down"  "touchend"    into-feed]
                           ["btn-down"  "mousedown"   into-feed]
                           ["btn-down"  "mouseup"     into-feed]

                           ["btn-left"  "touchstart"  into-feed]
                           ["btn-left"  "touchend"    into-feed]
                           ["btn-left"  "mousedown"   into-feed]
                           ["btn-left"  "mouseup"     into-feed]

                           ["btn-right" "touchstart"  into-feed]
                           ["btn-right" "touchend"    into-feed]
                           ["btn-right" "mousedown"   into-feed]
                           ["btn-right" "mouseup"     into-feed]

                           ;;buttons with single impulse
                           ["btn-a"     "click"       into-feed]
                           ["btn-b"     "click"       into-feed]
                           ["btn-select" "click"      into-feed]
                           ["btn-start" "click"       into-feed]
                           ["btn-configure" "click"   into-feed]
                           ["btn-connect" "click"     into-feed]])

        ;;-- push controller cought action to the server
        (send-actions outgoing-ch event-feed :controller)
        (on-connect event-feed :connection)
        ))))

(main)
