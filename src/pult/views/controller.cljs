(ns pult.views.controller
  (:refer-clojure :exclude [atom])
  (:require [reagent.core :as reagent :refer [atom cursor]]
            [clojure.string :as string]
            [cljs.core.async :as async]
            [pult.utils :refer [by-id]]))

(defn add-ctrl-events
  [event-ch svg-doc]
  (let [ctrl-events [["btn-up"    "touchstart"]
                     ["btn-up"    "touchend"]
                     ["btn-down"  "touchstart"]
                     ["btn-down"  "touchend"]
                     ["btn-left"  "touchstart"]
                     ["btn-left"  "touchend"]
                     ["btn-right" "touchstart"]
                     ["btn-right" "touchend"]
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

(defn mount-keyboard-events
  "listen keyboards events if it matches with activated binding
  then put it into controller event's feed."
  [app-state event-ch]
  (let [el js/document
        on-key (fn [ev]
                     (let [active-id (get-in @app-state [:profiles :active])
                           profile (get-in @app-state [:profiles :items active-id])
                           registered-key-codes (-> profile :mappings vals set)
                           to-key-code (fn [key-name]
                                         (-> key-name str string/upper-case keyword))]
                      (.preventDefault ev)
                      (if (contains? registered-key-codes (to-key-code (.-key ev)))
                        (async/put! event-ch {:source :controller :event ev})
                        (.log js/console "Not registered key - going to ignore it."))))]
    (.addEventListener el "keydown" on-key)
    (.addEventListener el "keyup" on-key)))

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
    (mount-keyboard-events global-app-state event-ch)
    (show event-ch ctrl-id)))

