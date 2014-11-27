(ns pult.views.connection
  (:refer-clojure :exclude [atom])
  (:require-macros [freactive.macros :refer [rx]])
  (:require [freactive.core :refer [atom cursor]]
            [freactive.dom :as dom]
            [cljs.core.async :as async]))

(defonce form-data (atom {:url "127.0.0.1"
                          :port "8080"
                          :path "ws"}))

(defn show-form
  [global-app-state]
  [:div
    {:class "pure-u-1"}
    (let [form-dt @form-data
          event-ch (:event-ch @global-app-state)]
      [:form
        {:id "connection-form"
         :class "pure-form pure-form-aligned"}
         [:fieldset
          [:div
            {:class "pure-control-group"}
            [:label {:for "url"} "Url"]
            [:input
              {:id "url" :name "url" :type "text"
               :class "pure-u-1-2"
               :value (str (:url form-dt))
               :on-input (fn [ev]
                           (let [new-val (str (.. ev -target -value))]
                             (reset! form-data
                                     (assoc form-dt :url new-val))))}]]
          [:div
            {:class "pure-control-group"}
            [:label {:for "port"} "Port"]
            [:input
              {:id "port" :name "port" :type "number"
               :class "pure-u-1-2"
               :value (int (:port form-dt))
               :on-input (fn [ev]
                           (reset! form-data
                                   (assoc form-dt :port (.. ev -target -value))))}]]
          [:div
            {:class "pure-control-group"}
            [:label {:for "path"} "Path"]
            [:input
              {:id "path" :name "path" :type "text"
               :class "pure-u-1-2"
               :value (str (:path form-dt))
               :on-input (fn [ev]
                           (reset! form-data
                                   (assoc form-dt :path (.. ev -target -value))))}]]
          [:div
            {:class "pure-controls"}
            [:button
              {:class "pure-button button-error pure-u-1"
               :type "button"
               :on-click (fn [e]
                            (.debug js/console "Connecting...")
                            (async/put! event-ch
                                        {:source :connection
                                         :data @form-data})
                            )}
              "Connect"]]]])])

(defn main [global-app-state]
  [:div
    {:width "100%" :height "100%"}
    [:h1 "Create new connection"]
    [:div {:id "connection-msg"}]
    [:div {:id "connection-form-container"}
      (show-form global-app-state)]])

