(ns pult.views.settings.mapping-form
  (:require [reagent.core :as reagent :refer [cursor]]
            [secretary.core :as secretary]
            [pult.utils :refer [current-time]]
            [pult.components.actions.menu :as menu-action]))

(def default-profile {:name "new"
                      :saved false
                      :description "Default OpenEMU binding for NES."
                      :mappings {"btn-up" :UP
                                 "btn-right" :RIGHT
                                 "btn-left" :LEFT
                                 "btn-down" :DOWN
                                 "btn-a" :A
                                 "btn-b" :S
                                 "btn-select" :SHIFT
                                 "btn-start" :ENTER}})
;src: https://docs.oracle.com/javase/7/docs/api/java/awt/event/KeyEvent.html
(def supported-keys [:UP :RIGHT :LEFT :DOWN :SHIFT :ENTER :SPACE :CONTROL
                     :A :B :C :D :E :F :G :H :I :J :K :L :M :N :O :P :Q :R
                     :S :T :U :V :W :X :Y :0 :1 :2 :3 :4 :5 :6 :7 :8 :9 :ADD
                     :BACK_SPACE :BRACE_LEFT :BRACE_RIGHT :ESCAPE :UNDEFINED])

(defn supported-keys-options []
  [:optgroup
    (for [key-code supported-keys]
      ^{:key (str key-code)} [:option {:value key-code} (name key-code)])])

(defn button-selector
  [btn-id btn-value]
  ^{:key btn-id} [:div
    {:class "pure-control-group"}
    [:label {:for "selector"}
      (str (name btn-value) ": ")]
    [:select
      {:id (str btn-id)
       :name (str btn-id)
       :class "pure-input-1"
       :defaultValue btn-value
       ;;TODO: check key is not already in use
       ;;TODO: update value after change ...
       :on-change #(.log js/console (str "Changed " btn-id))}
      (supported-keys-options)]])

(defn get-profile
  [profiles profile-id]
  (let [res (filter #(= profile-id (:name %1)) profiles)]
    (if (empty? res)
      default-profile
      (first res))))

(defn profile-form
  [profile-cur profile-id]
  (let [profile (get-profile (:items @profile-cur) profile-id)]
    (.log js/console (str "Profile-form: " (pr-str profile)))
    [:div {:class "pure-u-1"}
      [:form {:class "pure-form pure-form-stacked"}
        [:fieldset {:class "pure-group"}
          [:legend
            [:strong "Profile information"]]
          [:div {:class "pure-u-1"}
           [:label {:for "name"} "Name: "]
           [:input
              {:id "name" :name "name"
               :type "text"
               :class "pure-input-1 pure-input-lg-1-2 pure-input-md-1-2"
               :placeholder "Profile name"
               :defaultValue (:name profile)
               :on-change (fn [e]
                            (swap! profile
                                   #(assoc % :name (-> e .-target .-value))))}]]
          [:div {:class "pure-control-group"}
            [:label {:for "description"} "Description: "]
            [:textarea
              {:type "text"
               :class "pure-input-1"
               :defaultValue (:description profile)
               :placeholder "Profile description"}]]]

        [:fieldset
          {:class "pure-group"}
          [:legend [:strong "Controller keys"]]
          (for [[k v] (:mappings profile)]
            (button-selector k v))]
          ;;TODO: finish functionality
          [:button {:class "pure-button pure-button-primary pure-input-1"}  "Save"]
        ]]))

(defn profile-controls
  [profiles-cur profile-id]
  (let [active? (= profile-id (:active @profiles-cur))]
    [:div {:class "profile-controls"}
      [:button
        {:class "pure-button button-secondary pure-u-1-3"
         :on-click (fn [ev]
                     (.log js/console "Deleted mappings profile: " profile-id)
                     (swap! profiles-cur
                            (fn [xs]
                              (assoc xs
                                     :editing nil
                                     ;;if user deleted active profile
                                     :active (if active?
                                               ((comp :name first :items) xs)
                                               (:active xs))
                                     :items (vec (remove #(= profile-id (:name %)) (:items xs))))))
                     (secretary/dispatch! "/mappings"))}
        [:i {:class "fa fa-trash"} " "] "Delete"]
      [:button
        {:class "pure-button button-secondary pure-u-1-3"
         :on-click (fn [ev]
                     ;;TODO: add DB persistance
                     (.log js/console "user clones profile: " profile-id)
                     (let [items (:items @profiles-cur)
                           profile (get-profile items profile-id)
                           new-profile (assoc profile :name (str (:name profile)
                                                                 "_copy_"
                                                                 (current-time)))]
                       (swap! profiles-cur
                              (fn [xs]
                                (assoc xs :items (vec (cons new-profile (:items xs))))))
                       (secretary/dispatch! (str "/mappings/" (:name new-profile)))))}
        [:i {:class "fa fa-copy"} " "] "Clone"]
        [:button
         {:class "pure-button button-success pure-u-1-3"
          :style {:display (if active? "inline-block" "none")}
          ;;TODO: add DB persistance
          :on-click (fn [ev]
                      (let [cur profiles-cur
                            items (:items @cur)
                            others (remove #(= profile-id (:name %)) items)]
                        (.log js/console (str "Deactivated profile: " profile-id))
                        (swap! profiles-cur
                               (fn [xs]
                                 (assoc xs :active ((comp :name first) others))))))}
         [:i {:class "fa fa-power-off"} " "] "Deactivate"]
        [:button
          {:class "pure-button button-secondary pure-u-1-3"
           :style {:display (if active? "none" "inline-block")}
           :on-click (fn [ev]
                      (.log js/console "User activated profile: " profile-id)
                      (swap! profiles-cur (fn [xs] (assoc xs :active profile-id))))}
          [:i {:class "fa fa-power-off"} " "] "Activate"]]))

(defn render
  [app-state]
  (let [profiles-cur (cursor [:profiles] app-state)
        profile-id (:editing @profiles-cur)]
    [:div {:class "pure-u-1"}
     (menu-action/render
       [:h3 (str "Profile: " profile-id)]
       "#settings/mappings"
       [:div {:class "pure-u-1"}
        (profile-controls profiles-cur profile-id)
        (profile-form profiles-cur profile-id)])]
    ))
