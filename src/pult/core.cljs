(ns pult.core
  (:refer-clojure :exclude [atom])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [chord.client :refer [ws-ch]]
            [cljs.core.async :as async
                             :refer [<! >!]]
            [reagent.core :as reagent :refer [atom]]
            [reagent.cursor :refer [cursor]]
            [secretary.core :as secretary]
            [clojure.string :as string]
            [pult.db :as db]
            [pult.routes :as routes]
            [pult.models.history :as history-mdl]
            [pult.models.profiles :as profile-mdl]
            [pult.models.active-profile :as active-profile-mdl]
            [pult.utils :refer [current-time by-id by-tag-name
                                log error locate! vibrate!]]))

;history item: {:url "" :port "" :path ""}
;;TODO: add schema, specially history&profile items
;;pult.schemas.profiles/Profile ??
(defonce app-state (atom {:db {:name "pultdb"
                               :version 1
                               :connection nil}
                          :connection {:uri "ws://127.0.0.1:8080/ws"
                                       :socket nil
                                       :tab {:selected :prev}
                                       :history []}
                          :incoming-ch (async/chan 5)
                          :outgoing-ch (async/chan (async/sliding-buffer 100))
                          :event-ch (async/chan 10)
                          :profiles {:active nil
                                     :editing nil
                                     :items {} ;profile-mdl/default-profile
                                     :active-history []}
                          :author "TimGluz"
                          :client-id "pult-1" ;TODO: it should be GUID
                          }))

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
          (log "feedback lag: " (- (current-time) (:start message))))
        (error "Got error: " message))
      (recur))))

(defn start-messenger
  [url configs]
  (let [show-error (fn [id msg]
                     (let [err-el (by-id id)]
                       (.setAttribute err-el "style" "display:block;")
                       (set! (.-innerHTML err-el) (str "<p>" msg "</p>"))))]
  (go
    (let [{:keys [ws-channel error]} (<! (ws-ch url configs))
          incoming-ch (:incoming-ch @app-state)
          outgoing-ch (:outgoing-ch @app-state)]
      (if error
        (let [err-msg (str "Cant open a connection with the server "
                           "on `" url "` " (:reason error))]
          (.error js/console err-msg " -> " (pr-str error))
          (show-error "connection-msg" err-msg)
          false)
        (do
          (swap! app-state (fn [xs] (assoc-in xs [:connection :socket] ws-channel)))
          (listen-messages ws-channel incoming-ch)
          (send-messages ws-channel outgoing-ch)
          (handle-received-messages incoming-ch)
          (log "Connection opened successfully.")
          true))))))

(defn stop-messenger
  []
  (swap! app-state (fn [xs] (assoc-in xs [:connection :socket] nil))))

;;TODO: refactor
(defn on-disconnect
  [ev]
  (log "Closing connection.")
  (stop-messenger)
  (locate! "#connection"))

;;TODO: refactor show-error into own component with function close button&timeout
(defn connect
  [conn-dt]
  (let [{:keys [url port path]} conn-dt
        uri (str "ws://" url ":" port "/" path)]
    (go
      (if-let [succ (<! (start-messenger uri {:format :edn}))]
        (do
          (log "Connection success.")
          (vibrate! 10)
          (swap! app-state (fn [xs] (assoc-in xs [:connection :uri] uri)))
          (locate! "#controller"))
        (do
          (vibrate! 20)
          (error "Connection failure."))))))

(defn on-connect
  "listen connection event on event-feed"
  [event-feed source-id]
  (let [data-ch (async/chan 1)]
    (log "Waiting connection data.")
    (async/sub event-feed source-id data-ch)
    (go-loop []
      (when-let [conn-dt (<! data-ch)]
        (let [db (cursor [:db] app-state)
              history (cursor [:connection :history] app-state)]
          (connect (:data conn-dt))
          (history-mdl/add
            db
            (:data conn-dt)
            (fn [_]
              (history-mdl/get-n-latest db 7 #(reset! history %)))))
        (recur)))))

(defmulti event->action
  (fn [ev] (.-type ev)))

(defmethod event->action "click" [ev]
  {:action :key-press
   :duration 10
   :release? true})

(defmethod event->action "touchstart" [ev]
  {:action :key-press
   :duration 5
   :release? false})

(defmethod event->action "touchend" [ev]
  {:action :key-release
   :delay 5})

(defn to-key-code
  [key-name]
  (-> key-name str string/upper-case keyword))

(defmethod event->action "keyup" [ev]
  {:action :key-press
   :key (to-key-code (.-key ev))
   :release? false})

(defmethod event->action "keydown" [ev]
  {:action :key-release
   :key (to-key-code (.-key ev))})

(defmethod event->action :default [ev]
  (.error js/console "Unknown event - cant translate it to action." ev))


(defmulti on-special-key identity)

(defmethod on-special-key "btn-configure" [btn-id]
  (log "Showing pult settings")
  (locate! "#settings"))

(defmethod on-special-key "btn-connect" [btn-id]
  (log "Showing connection form")
  ;TODO: add disconnect
  (locate! "#connection"))

(defmethod on-special-key :default [btn-id]
  (error "on-special-key: unknown dispatch value - " (pr-str btn-id)))


;;TODO replace mapping with active mapping
(defn send-actions
  "listen controller actions on event channel and sends it to the server"
  [outgoing-ch event-feed source-id]
  (let [ctrl-ch (async/chan (async/sliding-buffer 50))
        profiles-cur (cursor [:profiles :items] app-state)]
    (async/sub event-feed source-id ctrl-ch)
    (log "Listening controller events ...")
    (go-loop []
      (when-let [dt (<! ctrl-ch)]
        (let [btn-id (.. (:event dt) -target -id)
              special-ids #{"btn-configure" "btn-connect"}
              client-id (get @app-state :client-id)
              active-profile-id (long (get-in @app-state [:profiles :active]))
              btn-code (get-in @profiles-cur
                               [active-profile-id :mappings (keyword btn-id)]
                               :UNDEFINED)]
          (if (contains? special-ids btn-id)
            (on-special-key btn-id) ;special key = dont send it to server
            (some->>
                  (:event dt)
                  event->action
                  (merge {:id :command
                          :client-id client-id
                          :key btn-code
                          :start (current-time)})
                  (async/put! outgoing-ch)))
          (vibrate! 20)
          (recur))))
    (log "Stopped listening controller actions.")))

(defn start-db
  "create db connection and after successful attempt loads initial data into
  global app-state;"
  [app-state]
  (let [db (cursor [:db] app-state)
        history (cursor [:connection :history] app-state)
        profiles (cursor [:profiles :items] app-state)
        keywordize-keys (fn [profile]
                          ;cljs-idx dont restore saved keywords as keys
                          (assoc profile
                                 :mappings (->> (:mappings profile)
                                          (map (fn [[k v]] [k (keyword v)]))
                                          (into {}))))
        add-profiles (fn [profiles-cur rows]
                        (reset! profiles-cur
                               (->> rows
                                  (map (fn [row] [(:id row) row])) ;index-table
                                  (map (fn [[k v]] [k (keywordize-keys v)]))
                                  (into {}))))
        add-active-profiles (fn [app-state rows]
                              (swap! app-state
                                     (fn [xs]
                                       (-> xs
                                       (assoc-in [:profiles :active] (-> rows first :id long))
                                       (assoc-in [:profiles :active-history] (vec rows))))))]
    (db/connect
      db
      (fn [conn]
        (.log js/console "Database is now connected.")
        (swap! db #(assoc % :connection conn))
        (history-mdl/get-n-latest db 7 #(reset! history %))
        (profile-mdl/get-all db (partial add-profiles profiles))
        (active-profile-mdl/get-n-latest db 7 (partial add-active-profiles app-state))
        ))))

(defn ^:export main []
  (let [outgoing-ch (:outgoing-ch @app-state)
        event-ch (:event-ch @app-state)
        event-feed (async/pub event-ch :source)]
      ;connect db & load initial data
      (start-db app-state)
      ;mount router
      (routes/mount app-state)
      ;;-- push controller cought action to the server
      (send-actions outgoing-ch event-feed :controller)
      (on-connect event-feed :connection)))

(main) ;FXOS doesnt allow inline code & dont include it for tests
