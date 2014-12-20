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


;;TODO: not-implemented fn in cljs-idx v.0.1.0
(defn upsert
  [db store-name row-dt success-fn]
  (let [tx (idx/make-tx (:connection @db) (clj->js store-name) true)
        store (.objectStore tx store-name)
        request (.put store (clj->js row-dt))]
    (set! (.-onsucess request) success-fn)
    (set! (.-onerror request) #(.error js/console "upsert error: " %))))


;;TODO: not-implemented fn in cljs-idx v.0.1.0
(defn remove-item
  [db store-name key-val success-fn]
  (when db
    (let  [tx (idx/make-tx (:connection @db) (clj->js store-name) true)
           store (.objectStore tx store-name)
           request (.delete store (clj->js key-val))]
      (set! (.-onsuccess request) success-fn)
      (set! (.-onerror request) #(.error js/console "remove-item error:" %)))))

;;TODO: not-implemented fn in cljs-idx v.0.1.0
(defn clear
  [db store-name success-fn]
  (let [req (-> (:connection @db)
               (idx/make-tx store-name true)
               (.objectStore store-name)
               (.clear))]
   (set! (.-onsuccess req) success-fn)
   (set! (.-onerror req) #(.error js/console "error with db/clear: " %))))

(defn delete-store
  [db store-name]
  (idx/delete-store (:connection @db) store-name))

