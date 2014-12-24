(ns pult.alert
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.units :refer [px]]
            [garden.color :refer [rgb hsl]]))

(defstyles styles
  [[:.pure-alert {:color "white"
                  :padding (px 5)
                  :border-radius "1px solid transparent"}]
   [:.pure-alert
    [:.close {:position "relative"
              :top (px -2)
              :right (px -21)
              :color "inherit"
              :background "inherit"
              :border (px 0)}]]
   [:.pure-success {:background (rgb 28 184 65)}]
   [:.pure-error {:background (rgb 202 60 60)}]
   [:.pure-warning {:background (rgb 223 117 20)}]
   [:.pure-secondary {:background (rgb 66 184 221)}]
   ])
