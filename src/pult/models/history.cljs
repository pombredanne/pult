(ns pult.models.history
  (:require [cljs-idxdb.core :as idx]
            [pult.utils :refer [current-time]]))

(def store-name "history")
(def store-index "timeIndex")

;;TODO: simple CRUD into OWN NS???
(defn add
  "adds a new connection item"
  ([db history-dt]
    (add db history-dt #(.log js/console (str "Success: new item in " store-name))))
  ([db history-dt success-fn]
    (if (empty? history-dt)
      (.error js/console "History data cant be empty or nil"))
      (idx/add-item (:connection @db)
                    store-name
                    (merge {:timestamp (current-time)} history-dt)
                    success-fn)))

(defn get-by
  "gets an item by its index"
  ([db index-val]
    (get-by db index-val #(.log js/console (str "Got: " (pr-str %)))))
  ([db index-val success-fn]
    (idx/get-by-index (:connection @db) store-name store-index index-val success-fn)))

(defn get-all
  "gets a whole list of connection history"
  ([db success-fn]
    (get-all db success-fn 0))
  ([db success-fn from-key]
    (idx/get-all (:connection @db) store-name from-key success-fn)))


(defn get-n-latest
  [db n success-fn]
  (get-all
    db
    (fn [rows]
      (->> rows
          (sort-by :timestamp >)
          (take n)
          (map :data)
          (vec)
          (success-fn)))))

(defn delete-all
  "cleans whole connection history"
  [db]
  (idx/delete-store (:connection @db) store-name))

