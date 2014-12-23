(ns pult.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:require [secretary.core :as secretary]
            [reagent.core :as reagent]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [pult.utils :refer [by-id locate!]]
            [pult.models.profiles :as profile-mdl]
            [pult.views.connection :as conn-app]
            [pult.views.controller :as ctrl-app]
            [pult.views.settings.core :as settings-view]
            [pult.views.settings.keymapping :as mapping-view]
            [pult.views.settings.mapping-form :as mapping-form]
            [pult.views.settings.changelog :as changelog-view]
            [pult.views.settings.license :as license-view])
  (:import goog.History))

(defn mount-connection-routes
  [app-state]
  (defroute connection-path "/connection" []
    (.log js/console "Rendering connection page")
    (reagent/render-component [#(conn-app/main app-state)] (by-id "app-container"))))

(defn mount-controller-routes
  [app-state]
  (defroute controller-path "/controller" []
    (.log js/console "Rendering pult")
     (reagent/render-component [#(ctrl-app/main app-state)] (by-id "app-container"))))

(defn mount-settings-routes
  [app-state]
  (defroute main-path "/settings" []
    (.log js/console "Rendering settings main page")
    (reagent/render-component [#(settings-view/render app-state)]
                              (by-id "app-container")))

  (defroute mappings-path "/settings/mappings" []
    (.log js/console "Showing keymappings page")
    (reagent/render-component [#(mapping-view/render app-state)]
                              (by-id "app-container")))

  (defroute mappings-form-path "/settings/mappings/:id" {id :id}
    (let [profile-id (long id)]
      (.log js/console "Showing mapping editor for: " profile-id)
      (if (= 0 profile-id)
        ;when user asked to add new profile from scratch
        (let [new-profile (profile-mdl/create {:saved? false})]
          (swap! app-state
                 #(assoc-in % [:profiles :items (:id new-profile)] new-profile))
          (swap! app-state #(assoc-in % [:profiles :editing] (:id new-profile))))
        ;for old profiles just swap editing number
        (swap! app-state (fn [xs] (assoc-in xs [:profiles :editing] profile-id))))

      (reagent/render-component [#(mapping-form/render app-state)]
                                (by-id "app-container"))))

  (defroute changelogs "/settings/changelogs" []
    (reagent/render-component [#(changelog-view/render app-state)]
                               (by-id "app-container")))
  (defroute licens "/settings/license" []
    (reagent/render-component [#(license-view/render app-state)]
                               (by-id "app-container")))
  )

(defn mount
  [app-state]
  (let [h (History.)]
    ;;-- start history
    (.log js/console "Starting secretary...")
    (secretary/set-config! :prefix "#")
    (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
    (.setEnabled h true)
    ;;-- mount routes
    (mount-connection-routes app-state)
    (mount-controller-routes app-state)
    (mount-settings-routes app-state)
    (locate! "#connection")))

