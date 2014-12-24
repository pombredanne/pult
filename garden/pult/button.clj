(ns pult.button
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.units :refer [px]]
            [garden.color :refer [hsl rgb rgb]]))

(defstyles styles
  [
    [:.button-success {:background-color (rgb 28 184 65)}]
    [:.button-error   {:background-color (rgb 202 60 60)}]
    [:.button-warning {:background-color (rgb 223 117 20)}]
    [:.button-secondary {:background-color (rgb 66 184 221)}]
    [:.button-success
     :.button-error
     :.button-warning
     :.button-secondary {:color "white"
                         :border-radius (px 0)
                         :text-shadow "0 1px 1px rgba(0,0,0,0.2)"}]])
