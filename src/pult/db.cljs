(ns pult.db
  (:require [cljs-idxdb.core :as idx]))

(def db-name "pultdb")
(def db-version 1)


;var request = window.indexedDB.deleteDatabase("pultDB");
(defn insert-seed-data!
  [db-tx]
  (.log js/console "Inserting seed data..")
  db-tx)

(defn run-migrations!
  [db-tx]
  (.log js/console "Executing DB migrations..")
  (-> db-tx
    (idx/create-store "history" {:keyPath "timestamp"})
    (idx/create-index "timeIndex" "timestamp" {:unique true}))
  (-> db-tx
      (idx/create-store "profiles" {:keyPath "name"})
      (idx/create-index "nameIndex" "name" {:unique true}))
  (-> db-tx
      (idx/create-store "profile-history" {:keyPath "timestamp"})
      (idx/create-index "timeIndex" "timestamp" {:unique true}))
  (insert-seed-data! db-tx))

(defn connect
  "initializes a new db connection
  Arguments:
    db - {:name \"db-name\" :version 1 :connection nil}
    success-fn - optional callback function
  "
  ([db]
    (connect db (fn [conn] (swap! db #(assoc % :connection conn)))))
  ([db success-fn]
    (idx/create-db (:name @db)
                   (:version @db)
                   run-migrations!
                   success-fn)
    db))


