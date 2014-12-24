(ns pult.action
  (require [garden.def :refer [defstyles]]
           [garden.units :refer [px]]))

(defstyles styles
  [[:.app-action
    [:h1 :h2 :h3 :h4 :h5 :h6 {:margin "5px 7px"}]

    [:.action-header {:background-color "steelblue"}
      [:.action-header-title {:color "white"}]]

    [:.action-list-item {}
      [:.to-right {:position "absolute"
                   :right (px 15)}]]]])
