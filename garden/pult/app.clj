(ns pult.app
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.units :refer [px em]]
            [pult.alert :as alert]
            [pult.button :as button]
            [pult.action :as action]))

(defstyles styles
  [[:.app-action {:overflow "hidden"
                  :min-height "100%"
                  :max-height "100%"}]
   [:#connection-form {:color "white"}]
   [:.legend {:color "white"}]
   alert/styles
   button/styles
   action/styles
   [:.inline-page {:width "100%"
                   :height "100%"
                   :border (px 0)
                   :position "fixed"}
      [:iframe {:width "100%"
                :height "100%"
                :border (px 0)}]]])
