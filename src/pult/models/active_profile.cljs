(ns pult.models.active-profile
  (:require [pult.utils :refer [current-time log error]]
            [pult.models.helpers :as h]))

(def store-name "profile-history")
(def store-index "timeIndex")

(defn add
  ([db profile-dt]
    (add db profile-dt #(log "Saved latest active profile" (:name profile-dt))))
  ([db profile-dt success-fn]
    (h/add db
           store-name
           {:timestamp (current-time)
            :id (:id profile-dt)
            :name (:name profile-dt)}
           success-fn)))

(defn get-by
  ([db timestamp]
    (get-by db timestamp #(log "active-profile/get-by: " (pr-str %))))
  ([db timestamp success-fn]
    (h/get-by db store-name store-index timestamp success-fn)))

(defn get-all
  [db success-fn]
  (h/get-all db store-name 0 success-fn))

(defn get-n-latest
  [db n success-fn]
  (get-all db
           (fn [rows]
             (->> rows
                  (sort-by :timestamp >)
                  (take n)
                  (vec)
                  (success-fn)))))
(defn delete-all
  "clears history of active profiles"
  ([db] (delete-all db #(log "Cleared all items from " store-name)))
  ([db success-fn]
    (h/clear db store-name success-fn)))
