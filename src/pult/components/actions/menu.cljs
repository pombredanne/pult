(ns pult.components.actions.menu
  (:require [reagent.core :as reagent]))

;;action-dt {:url "" :title ""}
(defn menu-list-item
  [url content selected?]
  ^{:key (str url)}
  [:li {:class (str "action-list-item "
                    (if selected? " pure-menu-selected" ""))}
    [:a {:href (str url)}
      (if selected?
        [:span {:class "to-left"}
          [:i {:class "fa fa-check"} " "] " "])
      [:span content]
      [:span {:class "to-right"}
        [:i {:class "fa fa-arrow-circle-o-right"} " "]]]])

;;TODO: add prismatic/schema
(defn menu-list-header
  [back-url content]
  [:div {:class "action-header pure-g"}
    [:div {:class "pure-u-1-8"}
      [:a {:href back-url
           :class "pure-button button-secondary"
           :title "Go back"}
        [:i {:class "fa fa-arrow-circle-o-left"} " "]]]
   [:div {:class "pure-u-7-8"}
      [:div {:class "action-header-title"} content]]])

(defn render
  [header back-url content]
  [:div {:class "app-action"}
    (menu-list-header back-url header)
    [:div {:class "action-content"}
      content]])

(defn render-list
  "renders menu action, which includes list of links"
  [header back-url menu-items]
  (render header back-url
    [:div {:class "action-content"}
      [:div {:class "pure-menu pure-menu-open"}
        [:ul
          (if (empty? menu-items)
            [:li [:a {:href "#new" :disabled "true"} "Add new profile"]]
            (map
              (fn [[url title selected?]] (menu-list-item url title selected?))
              menu-items))]]]))

