(ns pult.views.settings.mapping-form
  (:refer-clojure :exclude [atom])
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.cursor :refer [cursor]]
            [secretary.core :as secretary]
            [pult.utils :refer [current-time log error locate!]]
            [pult.models.profiles :as profile-mdl]
            [pult.models.active-profile :as active-profile-mdl]
            [pult.components.actions.menu :as menu-action]))

;;TODO: should default-profile & supported keys in own module? 
;;YES - if it's used somewhere else too; currently NO;
(def default-profile {:id 0 ;should change before saving a new model
                      :name "new"
                      :saved? false
                      :changed? true
                      :description "Please rename before saving!"
                      :mappings {:btn-up    :UP
                                 :btn-right :RIGHT
                                 :btn-left  :LEFT
                                 :btn-down  :DOWN
                                 :btn-a     :A
                                 :btn-b     :S
                                 :btn-select :SHIFT
                                 :btn-start :ENTER}})
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
  [btn-id btn-value on-change-fn validator-fn]
  (let [validation-error (atom nil)
        on-validation-error (fn [ev]
                              (let [new-key (-> ev .-target .-value)
                                    error-msg (str "Cant use the key "
                                                   "`"  new-key "`"
                                                   "- already in use")]
                                (error error-msg)
                                (reset! validation-error error-msg)))]
    (fn []
      [:div
        {:class "pure-control-group"}
        [:label {:for "selector"}
          (str btn-id " ")]
        [:select
          {:id (str btn-id)
           :name (str btn-id)
           :class "pure-input-1"
           :defaultValue btn-value
           :on-change (fn [ev]
                        (if (true? (validator-fn ev))
                          (on-change-fn ev)
                          (on-validation-error ev)))}
          (supported-keys-options)]
        ;--show validation errors when validator-fn fails
        [:div {:class "pure-alert pure-error"
               :style {:display (if (nil? @validation-error) "none" "block")}}
         [:p (str @validation-error)]]])))

(defn profile-form
  [db profiles-cur profile-id]
  (let [profile (cursor [:items profile-id] profiles-cur)
        selector-validator (fn [profile-cur old-val ev]
                             ;returns false if didnt pass validation
                             (let [key-val (-> ev .-target .-value keyword)
                                   reserved-vals (-> @profile-cur :mappings vals set)
                                   not-used? #(not (contains? %1 %2))]
                               (or
                                 (= old-val key-val)
                                 (not-used? reserved-vals key-val))))
        on-selector-change (fn [profile-cur key-id ev]
                             (let [new-key-val (-> ev .-target .-value keyword)]
                               (log "Updating selector value to: " new-key-val)
                               (swap! profile-cur
                                      (fn [xs]
                                        (-> xs
                                          (assoc :changed? true)
                                          (assoc-in [:mappings key-id] new-key-val))))))
        on-input-change (fn [profile-cur input-id ev]
                          (swap! profile-cur
                                 (fn [xs]
                                   (assoc xs
                                          :changed? true
                                          input-id (-> ev .-target .-value)))))
        on-save (fn [profile-cur ev]
                  (let [profile @profile-cur
                        profile-id (if (and
                                          (:saved? profile)
                                          (pos? (get profile :id 0)))
                                      (long (:id @profile-cur))
                                      (current-time))
                        new-profile (assoc profile
                                           :saved? true
                                           :changed? false
                                           :id profile-id)]
                    (.preventDefault ev) ; dont fire FORM submit event
                    (log "Saving profile:" profile-id ":" (:name profile))
                    (swap! profiles-cur
                           #(assoc-in % [:items profile-id] new-profile))
                    (profile-mdl/upsert
                      db new-profile
                      (fn [row]
                        (log "Saved new item: " (pr-str new-profile))))))]
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
               :defaultValue (:name @profile)
               :on-change (partial on-input-change profile :name)}]]
          [:div {:class "pure-control-group"}
            [:label {:for "description"} "Description: "]
            [:textarea
              {:type "text"
               :class "pure-input-1"
               :defaultValue (:description @profile)
               :placeholder "Profile description"
               :on-change (partial on-input-change profile :description)}]]]

        [:fieldset
          {:class "pure-group"}
          [:legend [:strong "Controller keys"]]
          (for [[k v] (:mappings @profile)]
            ^{:key (str k "_" v)} ;required meta-data for React
            [(button-selector k v
                             (partial on-selector-change profile k)
                             (partial selector-validator profile v))])]
          ;;TODO: does it updates already existing profile?
          [:button {:class "pure-button pure-button-primary pure-input-1"
                    :style {:display (if (:changed? @profile) "block" "none")}
                    :on-click (partial on-save profile)}
           [:span [:i {:class "fa fa-save"} " "] "Save"]]]]))

(defn profile-controls
  [db profiles-cur profile-id]
  (let [active? (= profile-id (:active @profiles-cur))
        profile (cursor [:items profile-id] profiles-cur)
        remove-profile (fn [xs]
                         (let [other-keys (remove #(= profile-id %)
                                                  (keys (:items @profiles-cur)))]
                           (assoc xs
                                  :editing nil
                                  :active (if (= profile-id (:active xs))
                                            ((comp first keys) other-keys)
                                            (:active xs))
                                  :items (dissoc (:items xs) profile-id))))
        on-activate (fn [db profiles-cur profile-id ev]
                      (let [cur profiles-cur
                            others (remove #(= profile-id (:id %))
                                           (:items @profiles-cur))
                            next-profile (first others)]
                        (log (str "Deactivated profile: " profile-id))
                        (active-profile-mdl/add db next-profile)
                        (swap! profiles-cur #(assoc % :active (:id next-profile)))))]
    [:div {:class "profile-controls"
           :style {:display (if (:saved? @profile) "block" "none")}}
      [:button
        {:class "pure-button button-secondary pure-u-1-3"
         :on-click (fn [ev]
                     (log "Deleting mappings profile: " profile-id)
                     (profile-mdl/delete-by db
                                            profile-id
                                            (fn [res]
                                              (log "deleted profile: " profile-id)
                                              (swap! profiles-cur remove-profile)
                                              (locate! "#settings/mappings"))))}
        [:i {:class "fa fa-trash"} " "] " Delete"]

      [:button
        {:class "pure-button button-secondary pure-u-1-3"
         :on-click (fn [ev]
                     (let [new-name (str (:name @profile) "_copy_" (current-time))
                           new-id (current-time)
                           new-profile (assoc @profile
                                              :name new-name
                                              :id new-id
                                              :saved? true
                                              :changed? false)]
                       (swap! profiles-cur #(assoc-in % [:items new-id] new-profile))
                       (locate! (str "#settings/mappings/" new-name))))}
        [:i {:class "fa fa-copy"} " "] " Clone"]

        [:button
         {:class "pure-button button-success pure-u-1-3"
          :style {:display (if active? "inline-block" "none")}
          :on-click (partial on-activate db profiles-cur profile-id)}
         [:i {:class "fa fa-power-off"} " "] "Deactivate"]
        [:button
          {:class "pure-button button-secondary pure-u-1-3"
           :style {:display (if active? "none" "inline-block")}
           :on-click (fn [ev]
                      (.log js/console "User activated profile: " profile-id)
                      (active-profile-mdl/add db @profile)
                      (swap! profiles-cur (fn [xs] (assoc xs :active profile-id))))}
          [:i {:class "fa fa-power-off"} " "] "Activate"]]))

(defn render
  [app-state]
  (let [db (cursor [:db] app-state)
        profiles-cur (cursor [:profiles] app-state)
        profile-id (long (:editing @profiles-cur))
        profile-cur (cursor [:profiles :items profile-id] app-state)]
    [:div {:class "pure-u-1"}
     (menu-action/render
       [:h3 {:data-profile-id profile-id}
            (str "Profile: " (:name @profile-cur)
                 (when-not (:saved? @profile-cur) " - unsaved")
                 (when (:changed? @profile-cur) "- unsaved"))]
       "#settings/mappings"
       [:div {:class "pure-u-1"}
        (profile-controls db profiles-cur profile-id)
        (profile-form db profiles-cur profile-id)])]))

