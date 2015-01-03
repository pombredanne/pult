(ns pult.models.settings
  (:require [pult.utils :refer [log error squuid]]
            [pult.models.helpers :as h]))

(def store-name "settings")
(def store-index "idIndex")

(defn add
  ([db key-id key-val]
    (add db key-id key-val #(log "Saved settings -  " key-id ": " (pr-str key-val))))
  ([db key-id key-val success-fn]
    (h/add db
           store-name
           {:id key-id
            :value key-val
            key-id key-val}
           success-fn)))

(defn get-by
  ([db key-id]
    (get-by db key-id #(log "mdl.settings/get-by: " (pr-str %))))
  ([db key-id success-fn]
    (h/get-by db store-name store-index key-id success-fn)))

(defn get-all
  [db success-fn]
  (h/get-all db store-name 0 success-fn))

(defn get-client-id
  [db success-fn]
  (get-by db "client-id" #(-> % first :client-id success-fn)))

(defn add-client-id
  "save client-id into DB"
  ([db client-id]
    (add-client-id db client-id #(log "Saved new client id:" client-id)))
  ([db client-id success-fn]
    (add db :client-id (str client-id) success-fn)))


(defn get-db-state
  [db success-fn]
  (get-by db "db-state" #(-> % first :db-state success-fn)))

(defn add-db-state
  ([db initialized?]
    (add db :db-state initialized? #(log "Saved new db-state: " initialized?)))
  ([db initialized? success-fn]
    (add db :db-state (true? initialized?) success-fn)))

