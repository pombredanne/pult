(ns pult.settings
  (:refer-clojure :exclude [atom])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [secretary.core :refer [defroute]])
  (:require [cljs.core.async :as async :refer [<! >!]]
            [reagent.core :as reagent :refer [atom cursor]]
            [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [pult.db :as db]
            [pult.utils :refer [by-id]]
            [pult.components.actions.menu :as menu-action]
            [pult.views.settings.keymapping :as mapping-view]
            [pult.views.settings.mapping-form :as mapping-form])
  (:import goog.History))

(defonce app-state (atom {:id "uuid-1-2"
                          :profiles {:editing nil
                                     :active "profile1"
                                     :items [{:name "profile1"
                                              :description "Mocked profile"}
                                             {:name "profile2"
                                              :description "mocked profile.2"}]}}))

;;TODO: add action-view
(defn settings-view
  [app-state]
  [:div {:class "pure-u-1 pure-xl-1-3 pure-lg-1-2"}
    (menu-action/render-list
      [:h3 "Settings"]
      "#"
      [["#mappings" "Keymappings"]
       ["#credits"  "Credits"]
       ["#license"  "License"]
       ["#feedback" "Feedback"]])])

;;TODO: into routes.cljs
#_(defroute main-path "/" []
  (.log js/console "Rendering settings main page")
  (reagent/render-component
    [#(settings-view app-state)]
    (by-id "app-container")))

#_(defroute mappings-path "/mappings" []
  (.log js/console "Showing keymappings page")
  (reagent/render-component
    [#(mapping-view/render app-state)]
    (by-id "app-container")))

#_(defroute mappings-form-path "/mappings/:id" {profile-id :id}
  (.log js/console "Showing mapping editor for: " profile-id)
  (swap! app-state (fn [xs] (assoc-in xs [:profiles :editing] profile-id)))
  (reagent/render-component
    [#(mapping-form/render app-state)]
    (by-id "app-container")))

#_(defn ^:export main []
  (.debug js/console "Registering components...")
  )


