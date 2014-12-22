(ns pult.views.settings.core
  (:require [reagent.core :as reagent :refer [cursor]]
            [pult.components.actions.menu :as menu-action]))

(defn render
  [app-state]
  [:div {:class "pure-u-1 pure-xl-1-3 pure-lg-1-2"}
    (menu-action/render-list
      [:h3 "Settings"]
      "#connection"
      [["#settings/mappings" "Keymappings"]
       ["#settings/changelogs" "Changelogs"]
       ["#settings/license"  "License"]
       ["#settings/feedback" "Feedback"]])])
