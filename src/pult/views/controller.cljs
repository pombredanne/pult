(ns pult.views.controller
  (:refer-clojure :exclude [atom])
  (:require [reagent.core :as reagent
                          :refer [atom cursor]]
            [cljs.core.async :as async]
            [pult.utils :refer [by-id]]))

(defn add-ctrl-events
  [event-ch svg-doc]
  (let [ctrl-events [["btn-up"    "touchstart"]
                     ["btn-up"    "touchend"]
                     ["btn-up"    "mousedown"]
                     ["btn-up"    "mouseup"]
                     ["btn-down"  "touchstart"]
                     ["btn-down"  "touchend"]
                     ["btn-down"  "mousedown"]
                     ["btn-down"  "mouseup"]
                     ["btn-left"  "touchstart"]
                     ["btn-left"  "touchend"]
                     ["btn-left"  "mousedown"]
                     ["btn-left"  "mouseup"]
                     ["btn-right" "touchstart"]
                     ["btn-right" "touchend"]
                     ["btn-right" "mousedown"]
                     ["btn-right" "mouseup"]
                     ;;buttons with single impulse
                     ["btn-a"     "click"]
                     ["btn-b"     "click"]
                     ["btn-select" "click"]
                     ["btn-start" "click"]
                     ["btn-configure" "click"]
                     ["btn-connect" "click"]]
        into-feed (fn [ev]
                    (async/put! event-ch
                                {:source :controller
                                 :event ev}))]
    (.log js/console "Registering controller events.")
    (doseq [[selector action-name] ctrl-events]
      (if-let [target (by-id svg-doc selector)]
        (.addEventListener target action-name into-feed)
        (.error js/console
                (str "Failed to register event for:" selector " , " action-name))))))

(defn mount-ctrl-events [event-ch elems]
  (with-meta (fn [] elems)
    {:component-did-mount (fn [this]
                            (.debug js/console "Loading SVG doc")
                            (let [el (reagent/dom-node this)]
                              ;;add ctrller events after svg image is loaded
                              (.addEventListener el "load"
                                (fn [ev]
                                 (.debug js/console "SVG doc is loaded.")
                                 (add-ctrl-events event-ch (.-contentDocument el))))
                              this))}))

(defn show
  [event-ch ctrl-id]
  (let [X (.-width js/window.screen)
        Y (.-height js/window.screen)]
    [(mount-ctrl-events
       event-ch
       [:object
         {:id ctrl-id
          :type "image/svg+xml"
          :data "img/control.svg"
          :width (str (max X Y))
          :height (str (min X Y))}
        "Your browser doesnt support SVG!"])]))

(defn main [global-app-state]
  (let [ctrl-id "control-object"
        event-ch (:event-ch @global-app-state)]
    (show event-ch ctrl-id)))

