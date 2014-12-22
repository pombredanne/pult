(ns pult.views.settings.license
  (:require [reagent.core :as reagent]
            [pult.components.actions.menu :as menu-action]))

(defn render
  [app-state]
  [:div {:class "pure-u-1"}
    (menu-action/render
      [:h3 "License"]
      "#settings"
      [:div {:class "inline-page"}
        [:iframe {:src "static/license.html"
                  :seamless true}
          "Here you should see software license."]])])
