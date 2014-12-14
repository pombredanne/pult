(ns pult.core
  (:refer-clojure :exclude [atom])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [chord.client :refer [ws-ch]]
            [cljs.core.async :as async
                             :refer [<! >!]]
            [reagent.core :as reagent :refer [atom cursor]]
            [pult.db :as db]
            [pult.models.history :as history-mdl]
            [pult.utils :refer [current-time by-id by-tag-name
                                hide-by-id! show-by-id!]]
            [pult.views.connection :as conn-app]
            [pult.views.controller :as ctrl-app]))

;history item: {:url "" :port "" :path ""}

(defonce app-state (atom {:db {:name "pultdb"
                               :version 1
                               :connection nil}
                          :connection {:uri "ws://127.0.0.1:8080/ws"
                                       :socket nil
                                       :tab {:selected :prev}
                                       :history []}
                          :incoming-ch (async/chan 5)
                          :outgoing-ch (async/chan (async/sliding-buffer 5))
                          :event-ch (async/chan 10)
                          :mappings {"btn-up" :UP
                                     "btn-right" :RIGHT
                                     "btn-left" :LEFT
                                     "btn-down" :DOWN
                                     "btn-a" :A
                                     "btn-b" :S
                                     "btn-select" :SHIFT
                                     "btn-start" :ENTER}
                          :settings {:editing-profile ""
                                     :profiles {"new" {:name "new"
                                                       :mappings {"btn-up" :UP}}}}
                          }))

;;-- helpers
(defn vibrate!
  [duration]
  (.vibrate js/navigator duration))

;;-- message handlers
(defn listen-messages
  "listend incoming messages from server"
  [ws-channel incoming-ch]
  (async/pipe ws-channel incoming-ch false))

(defn send-messages
  [ws-channel outgoing-ch]
  (async/pipe outgoing-ch ws-channel false))

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
          (.error js/console "Cant open connection with the server." (pr-str error))
          false)
        (do
          (swap! app-state (fn [xs] (assoc-in xs [:connection :socket] ws-channel)))
          (listen-messages ws-channel incoming-ch)
          (send-messages ws-channel outgoing-ch)
          (handle-received-messages incoming-ch)
          (.debug js/console "Connection opened successfully.")
          true)))))

(defn stop-messenger
  []
  (swap! app-state (fn [xs] (assoc-in xs [:connection :socket] nil))))

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
          (swap! app-state (fn [xs] (assoc-in xs [:connection :uri] uri)))
          (hide-by-id! "connection")
          (show-by-id! "controller"))
        (do
          (.debug js/console "Connection failure.")
          (show-error "connection-msg" "Connection failure!"))))))

(defn on-connect
  "listen connection event on event-feed"
  [event-feed source-id]
  (let [data-ch (async/chan 1)]
    (.debug js/console "Waiting connection data.")
    (async/sub event-feed source-id data-ch)
    (go-loop []
      (when-let [conn-dt (<! data-ch)]
        (let [db (cursor [:db] app-state)
              history (cursor [:connection :history] app-state)]
          (connect conn-dt)
          (history-mdl/add
            db conn-dt
            (fn [_]
              (history-mdl/get-n-latest db 10 #(reset! history %)))))
        (recur)))))

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

(defn start-db
  [app-state]
  (let [db (cursor [:db] app-state)
        history (cursor [:connection :history] app-state)]
    (db/connect
      db
      (fn [conn]
        (.log js/console "Database is now connected.")
        (swap! db #(assoc % :connection conn))
        (history-mdl/get-n-latest db 10 #(reset! history %))))))

(defn ^:export main []
  (let [outgoing-ch (:outgoing-ch @app-state)
        event-ch (:event-ch @app-state)
        event-feed (async/pub event-ch :source)]
      ;connect db & load initial data
      (start-db app-state)

      (.debug js/console "Registering components...")
      ;;-- init app views
      (reagent/render-component [#(conn-app/main app-state)] (by-id "connection"))
      (reagent/render-component [#(ctrl-app/main app-state)] (by-id "controller"))

      ;;-- push controller cought action to the server
      (send-actions outgoing-ch event-feed :controller)
      (on-connect event-feed :connection)))

;TODO: controll views with secretary
;(main)
