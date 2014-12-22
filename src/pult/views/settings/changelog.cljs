(ns pult.views.settings.changelog
  (:require [reagent.core :as reagent]
            [pult.components.actions.menu :as menu-action]))

(defn render
  [app-state]
  [:div {:class "pure-u-1"}
    (menu-action/render
      [:h3 "Changelogs"]
      "#settings"
      [:iframe {:class "inline-page"
                :src "static/changelogs.html"
                :seamless true}
        "Here should be changelogs."])])
