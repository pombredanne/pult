(ns pult.db
  (:require [cljs-idxdb.core :as idx]
            [pult.utils :refer [log error current-time squuid]]
            [pult.models.profiles :as profile-mdl]
            [pult.models.active-profile :as active-profile-mdl]
            [pult.models.settings :as settings-mdl]))

(def db-name "pultdb")
(def db-version 1)

;var request = window.indexedDB.deleteDatabase("pultDB");
(defn add-seed-data!
  [db success-fn]
  (log "Inserting seed data..")
  (let [new-id (long (current-time))]
    (profile-mdl/add
      db
      (profile-mdl/create {:saved? true
                           :changed? false
                           :id new-id
                           :name "NES_remote1"
                           :description "default NES bindings on OpenEmu"}))
    (active-profile-mdl/add db {:id new-id :name "NES_remote1"})
    (settings-mdl/add-client-id db (str (squuid)))
    (settings-mdl/add-db-state db true success-fn)
    (log "DB has now seed data;")
    db))

(defn run-migrations!
  [db-tx]
  (log "Executing DB migrations..")
  (-> db-tx
      (idx/create-store "history" {:keyPath "timestamp"})
      (idx/create-index "timeIndex" "timestamp" {:unique true}))
  (-> db-tx
      (idx/create-store "profiles" {:keyPath "name"})
      (idx/create-index "idIndex" "id" {:unique true}))
  (-> db-tx
      (idx/create-store "profile-history" {:keyPath "timestamp"})
      (idx/create-index "timeIndex" "timestamp" {:unique true}))
  (-> db-tx
      (idx/create-store "settings" {:keyPath "id"})
      (idx/create-index "idIndex" "id" {:unique true}))
  (log "DB migration is done.")
  db-tx)

(defn delete-all
  [db-tx]
  (log "Deleting db")
  (.deleteDatabase js/indexedDB db-name))

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


