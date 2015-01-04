(ns pult.models.profiles
  (:require [pult.utils :refer [current-time log error]]
            [pult.models.helpers :as h]))

(def store-name "profiles")
(def store-index "idIndex")
(def default-profile {:id 0
                      :name "new"
                      :saved? false ;should be false when cloning or creating new
                      :description "Please rename before saving!"
                      :mappings {:btn-up    :UP
                                 :btn-right :RIGHT
                                 :btn-left  :LEFT
                                 :btn-down  :DOWN
                                 :btn-a     :A
                                 :btn-b     :S
                                 :btn-select :SHIFT
                                 :btn-start :ENTER}})

(defn create
  [opts]
  (merge default-profile
         {:id (current-time)}
         opts))

;;TODO: add profile schema and validation
(defn add
  ([db profile-dt]
    (add db profile-dt #(log "Added new profile:" (pr-str %))))
  ([db profile-dt success-fn]
    (h/add db store-name profile-dt success-fn)))

(defn upsert
  ([db profile-dt]
    (upsert db profile-dt #(log "Upserted profile" (pr-str %))))
  ([db profile-dt success-fn]
    (h/upsert db store-name profile-dt success-fn)))

(defn get-by
  ([db profile-name]
    (get-by db profile-name #(log "Got: " (pr-str %))))
  ([db profile-name success-fn]
    (h/get-by db store-name store-index profile-name success-fn)))

(defn get-all
  "returns a list of profiles"
  [db success-fn]
  (h/get-all db store-name 0 success-fn))

(defn delete-by
  ([db profile-id]
    (delete-by db profile-id #(log "Success: deleted profile " (pr-str %))))
  ([db profile-id success-fn]
    (h/remove-item db store-name profile-id success-fn)))

(defn delete-all
  ([db] (delete-all #(log "Deleted all items on " store-name)))
  ([db success-fn]
    (h/clear db store-name success-fn)))

