"Helper functions for the models"
(ns pult.models.helpers
  (:require [cljs-idxdb.core :as idx]
            [pult.utils :refer [log error]]))

(defn add
  ([db store-name row-dt]
    (add db store-name row-dt #(log "Success: new item in " store-name)))
  ([db store-name row-dt success-fn]
    (if (empty? row-dt)
      (.error js/console "Can't add empty data into " store-name)
      (idx/add-item (:connection @db) store-name row-dt success-fn))))

(defn get-by
  "gets an data-row by its index"
  [db store-name index-name index-val success-fn]
  (idx/get-by-index (:connection @db) store-name index-name index-val success-fn))

(defn get-all
  "returns all data from the data store"
  [db store-name from-key success-fn]
  (idx/get-all (:connection @db) store-name from-key success-fn))

(defn delete-store
  [db store-name]
  (idx/delete-store (:connection @db) store-name))

