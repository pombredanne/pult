(ns pult.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [chord.client :refer [ws-ch]]
            [cljs.core.async :as async
                             :refer [<! >!]]))

(defn by-id
  [dom-obj id]
  (.getElementById dom-obj id))

(defn vibrate!
  [duration]
  (.vibrate js/navigator duration))

(defn on-configure
  [ev]
  (.debug js/console "user clicked on config..."))

(defn on-action1
  [ev]
  (vibrate! 1000)
  (.debug js/console "got controller action")
  (.debug js/console ev))

(defn on-select
  [ev]
  (.debug js/console "pushed select btnj"))

(defn on-start
  [ev]
  (.debug js/console "pushed start"))

(defn register-events!
  [doc-obj selector-actions]
  (doseq [[selector action-name action-fn] selector-actions]
    (if-let [target (by-id doc-obj selector)]
      (.addEventListener target action-name action-fn)
      (.error js/console
              (str "Failed to register event for:" selector " , " action-name)))))

(defn listen-messages
  [ws-channel incoming-ch]
  (async/pipe ws-channel incoming-ch false))

(defn send-messages
  [ws-channel outgoing-ch]
  (async/pipe outgoing-ch ws-channel false))

;;TODO: handle-received-messages


(defn start-messenger
  [url configs incoming-ch outgoing-ch]
  (go
    (let [{:keys [ws-channel error]} (<! (ws-ch url configs))]
      (if error
        (.error js/console "Cant open connection with the server." error)
        (do
          (listen-messages ws-channel incoming-ch)
          (send-messages ws-channel outgoing-ch)
          (.debug js/console "Connection opened successfully."))))))


(defn ^:export main
  []
  (let [control-obj (by-id js/document "control-object")
        svg-doc (.-contentDocument control-obj)
        ws-url "ws://127.0.0.1:8080/ws"
        ws-configs {:format :json}
        incoming-ch (async/chan 10)
        outgoing-ch (async/chan (async/sliding-buffer 10))
        on-action (fn [ev]
                    (.debug js/console "user clicked on action button" ev)
                    (async/put! outgoing-ch {:id "command"}))]
    (if (nil? svg-doc)
      (.error js/console "Failed to load controller UI.")
      (do
        (.debug js/console "Starting connection")
        (start-messenger ws-url {} incoming-ch outgoing-ch)

        (.debug js/console "Registering button actions...")
        (register-events! svg-doc
                          [["btn-up"    "click" on-action]
                           ["btn-down"  "click" on-action]
                           ["btn-left"  "click" on-action]
                           ["btn-right" "click" on-action]
                           ["btn-a"     "click" on-action]
                           ["btn-b"     "click" on-action]
                           ["btn-select" "click" on-select]
                           ["btn-start" "click" on-start]
                           ["btn-configure" "click" on-configure]])
        ;;TODO: refactor it
        (go-loop []
          (when-let [msg (<! incoming-ch)]
            (.debug js/console "Got new message: " msg)
            (recur))
          )))))

(main)
