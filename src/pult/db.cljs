(ns pult.db
  (:require [cljs-idxdb.core :as idx]
            [pult.utils :refer [log error current-time]]
            [pult.models.profiles :as profile-mdl]
            [pult.models.active-profile :as active-profile-mdl]))

(def db-name "pultdb")
(def db-version 1)

;var request = window.indexedDB.deleteDatabase("pultDB");
(defn insert-seed-data!
  [db-conn]
  (.log js/console "Inserting seed data..")
  (let [new-id (long (current-time))
        db (atom  {:connection db-conn})
        default-profile (profile-mdl/create {:saved? true
                                             :changed? false
                                             :id new-id
                                             :name "NES_remote1"
                                             :description "default NES bindings on OpenEmu"})]
    (profile-mdl/add db default-profile)
    (active-profile-mdl/add db {:id new-id :name "NES_remote1"})
    db-conn))

(defn run-migrations!
  [db-tx]
  (.log js/console "Executing DB migrations..")
  (-> db-tx
    (idx/create-store "history" {:keyPath "timestamp"})
    (idx/create-index "timeIndex" "timestamp" {:unique true}))
  (-> db-tx
      (idx/create-store "profiles" {:keyPath "name"})
      (idx/create-index "idIndex" "id" {:unique true}))
  (-> db-tx
      (idx/create-store "profile-history" {:keyPath "timestamp"})
      (idx/create-index "timeIndex" "timestamp" {:unique true}))
  (try
    (insert-seed-data! db-tx)
    (catch js/Object e
      (.error js/console "failed to insert seed data." e))))

(defn delete-all
  [db-tx]
  (.log js/console "Deleting db")
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


