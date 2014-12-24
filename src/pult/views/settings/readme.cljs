(ns pult.views.settings.readme
  (:require [reagent.core :as reagent]
            [pult.components.actions.menu :as menu-action]))

(defn render
  [app-state]
  [:div {:class "pure-u-1"}
    (menu-action/render
      [:h3 "Readme"]
      "#settings"
      [:div {:class "inline-page"}
        [:iframe {:src "static/readme.html"
                  :seamless true}
          "Here you should see readme file."]])])

