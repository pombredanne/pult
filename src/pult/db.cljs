(ns pult.db
  (:require [cljs-idxdb.core :as idx]))

(def db-name "pultdb")
(def db-version 1)



;;TODO: add better migration function
(defn connect
  "initializes a new db connection
  Arguments:
    db - {:name \"db-name\" :version 1 :connection nil}
    success-fn - optional callback function
  "
  ([db]
    (connect db
             (fn [conn] (swap! db #(assoc % :connection conn)))))
  ([db success-fn]
    (idx/create-db
      (:name @db)
      (:version @db)
      ;migration fn when upgrade-needed event is called
      #(-> (idx/create-store % "history" {:keyPath "timestamp"})
           (idx/create-index "timeIndex" "timestamp" {:unique true}))
      success-fn)
    db))


