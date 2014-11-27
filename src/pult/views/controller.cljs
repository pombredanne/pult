(ns pult.views.controller
  (:refer-clojure :exclude [atom])
  (:require-macros [freactive.macros :refer [rx]])
  (:require [freactive.core :refer [atom cursor]]
            [freactive.dom :as dom]
            [cljs.core.async :as async]
            [pult.utils :refer [by-id]]))

(defn register-events!
  [doc-obj selector-actions]
  (doseq [[selector action-name action-fn] selector-actions]
    (if-let [target (by-id doc-obj selector)]
      (.addEventListener target action-name action-fn)
      (.error js/console
              (str "Failed to register event for:" selector " , " action-name)))))

(defn add-ctrl-events
  [event-ch svg-doc]
  (let [
        ;svg-doc (.-contentDocument ctrl-node)
        into-feed (fn [ev]
                    (async/put! event-ch
                                {:source :controller
                                 :event ev}))]
    (register-events! svg-doc
                      [["btn-up"    "touchstart"  into-feed]
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
                       ["btn-connect" "click"     into-feed]])))

(defn show
  [event-ch ctrl-id]
  (let [width (.-width js/window.screen)
        height (.-height js/window.screen)]
    (dom/with-transitions
      [:object
        {:id ctrl-id
         :type "image/svg+xml"
         :data "img/control.svg"
         :width (str width)
         :height (str height)}
        "Your browser doesnt support SVG!"]
      {:on-show (fn [node cb]
                  (dom/listen! node "load"
                               (fn [ev]
                                 (.debug js/console "SVG doc is loaded.")
                                 (add-ctrl-events event-ch (.-contentDocument node))))
                  )})))

(defn main [global-app-state]
  (let [ctrl-id "control-object"
        event-ch (:event-ch @global-app-state)
        ;node (show event-ch ctrl-id)
        ]
    (show event-ch ctrl-id)
    ;(.debug js/console (dom/get-virtual-dom node))
    ))

