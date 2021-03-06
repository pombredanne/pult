(ns pult.views.connection
  (:refer-clojure :exclude [atom])
  (:require [reagent.core :as reagent
                          :refer [atom cursor]]
            [cljs.core.async :as async]
            [cljs-time.core :as time]
            [cljs-time.coerce :as time-coerce :refer [from-long to-local-date-time]]
            [pult.components.actions.menu :refer [menu-list-header]]))

(defonce form-data (atom {:url "127.0.0.1"
                          :port "8080"
                          :path "ws"}))

(defn show-form
  [global-app-state]
  [:div
    {:class "pure-u-1 app-action"}
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
               :on-change (fn [ev]
                           (let [new-val (str (-> ev .-target .-value))]
                             (reset! form-data
                                     (assoc form-dt :url new-val))))}]]
          [:div
            {:class "pure-control-group"}
            [:label {:for "port"} "Port"]
            [:input
              {:id "port" :name "port" :type "number"
               :class "pure-u-1-2"
               :value (int (:port form-dt))
               :on-change (fn [ev]
                           (reset! form-data
                                   (assoc form-dt :port (-> ev .-target .-value))))}]]
          [:div
            {:class "pure-control-group"}
            [:label {:for "path"} "Path"]
            [:input
              {:id "path" :name "path" :type "text"
               :class "pure-u-1-2"
               :value (str (:path form-dt))
               :on-change (fn [ev]
                           (reset! form-data
                                   (assoc form-dt :path (-> ev .-target .-value))))}]]
          [:div
            {:class "pure-controls"}
            [:button
              {:class "pure-button button-error pure-u-1"
               :type "button"
               :on-click (fn [e]
                            (.debug js/console "Connecting...")
                            (async/put! event-ch
                                        {:source :connection
                                         :data @form-data}))}
              "Connect"]]]])])

(defn show-previous [global-app-state]
  (let [history (cursor [:connection :history] global-app-state)
        event-ch (:event-ch @global-app-state)]
    [:div {:class "pure-u-1"}
      (if (empty? @history)
       [:span {:style {:text-align "center"}}
        [:i {:class "fa fa-info-circle"
             :style {:margin "0 45%"
                     :color "orange"
                     :font-size "3.5em"}} " "]
        [:br]
        [:h2 " No previous connection."]
        [:p "Please add new connection."]]
       ;if there's data
       [:div {:class "pure-menu pure-menu-open"}
         [:ul
          (for [item @history]
            ^{:key (str (:timestamp item) "_" (rand-int 100))}
            [:li
              [:a {:href "#" :class ""
                   :on-click (fn [ev]
                               (.log js/console "Connecting with " (pr-str item))
                               (async/put! event-ch
                                           {:source :connection
                                            :data item}))}
                [:span
                  (str "ws://" (:url item)
                       ":" (:port item)
                       "/" (:path item))
                  [:small
                    (str " - "
                      (-> item :timestamp from-long to-local-date-time time-coerce/to-string))]
                  ]]])]])]))

(defn main [global-app-state]
  (let [tab (cursor [:connection :tab] global-app-state)
        selected? (fn [tab-id]
                    (= tab-id (:selected @tab)))]
    [:div {:class "connection-container app-action pure-g"}
      (menu-list-header
        "#"
        [:span
          [:h3 {:class "pull-left"} "Connection"]
          [:a {:href "#settings"
             :class "pure-button button-secondary pull-right"}
            [:i {:class "fa fa-cogs"} " "]]])
      [:div {:id "connection-menu"
             :class "pure-u-1"}
        [:button
         (merge {:class "pure-button pure-u-1-2"
                 :on-click (fn [ev]
                             (.log js/console "user clicked on history")
                             (reset! tab {:selected :prev})
                             (.log js/console (pr-str @tab)))}
                (if (selected? :prev)
                  {:disabled true}
                  {:style {:color "steelblue"}}))
          [:i {:class "fa fa-keyboard-o"} " "]
          "History"]
        [:button
          (merge {:class "pure-button pure-u-1-2"
                  :on-click #(reset! tab {:selected :new})}
                 (if (selected? :new)
                    {:disabled true}
                    {:style {:color "steelblue"}}))
          [:i {:class "fa fa-hdd-o"} " "]
          "New connection"]]
      [:div {:id "connection-msg"
             :class "pure-u-1 pure-alert pure-error"
             :style {:display "none"}} ""]
      [:div {:id "connection-form-container"
             :class "pure-u-1"}
       (case (:selected @tab)
         :new (show-form global-app-state)
         (show-previous global-app-state))]]))

