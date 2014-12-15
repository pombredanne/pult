(ns pult.views.settings.core
  (:require [reagent.core :as reagent :refer [atom cursor]]
            [pult.components.actions.menu :as menu-action]))

(defn render
  [app-state]
  [:div {:class "pure-u-1 pure-xl-1-3 pure-lg-1-2"}
    (menu-action/render-list
      [:h3 "Settings"]
      "#"
      [["#settings/mappings" "Keymappings"]
       ["#settings/credits"  "Credits"]
       ["#settings/license"  "License"]
       ["#settings/feedback" "Feedback"]])])
