(ns pult.views.settings.keymapping
  (:refer-clojure :exclude [atom])
  (:require [reagent.core :as reagent :refer [atom cursor]]
            [pult.components.actions.menu :as menu-action]))
;;TODO: fix - saved activation doesnt work
(defn render
  [app-state]
  (let [profiles-cur (cursor [:profiles] app-state)
        active-profile-id (:active @profiles-cur)]
    (fn []
      [:div {:class "pure-u-1 pure-xl-1-3"}
        (menu-action/render-list
          [:span
            [:h3 {:class "pull-left"} "Keymappings"]
            [:a
              {:href "#settings/mappings/0"
               :class "pure-button button-secondary pull-right"}
              [:i {:class "fa fa-plus"}]]]
          "#settings"
          (vec (map
                 (fn [{:keys [name id]}]
                   [(str "#settings/mappings/" id) (str name) (= id active-profile-id)])
                 (vals (:items @profiles-cur)))))])))

