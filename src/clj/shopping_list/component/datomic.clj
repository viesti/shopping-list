(ns shopping-list.component.datomic
  (:require [com.stuartsierra.component :as component]
            [datomic.api :as d]))

(def inc-count
  (d/function
   '{:lang "clojure"
     :params [db name]
     :require [[datomic.api :as d]]
     :code (if-let [[id count] (first (d/q '[:find ?e ?count
                                             :in $ ?name
                                             :where
                                             [?e :item/name ?name]
                                             [?e :item/count ?count]]
                                           db
                                           name))]
             [[:db/add id :item/count (inc count)]]
             [{:db/id #db/id[:db.part/user]
               :item/name name
               :item/count 1}])}))

(def dec-count
  (d/function
   '{:lang "clojure"
     :params [db id]
     :require [[datomic.api :as d]]
     :code (if-let [[id count] (first (d/q '[:find ?e ?count
                                             :in $ ?e
                                             :where
                                             [?e :item/count ?count]]
                                           db
                                           id))]
             [[:db/add id :item/count (dec count)]]
             (throw (ex-info (str "Entity " id " does not exist")
                             {:id id})))}))

(defn init [uri]
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (d/transact
     conn
     [{:db/id (d/tempid :db.part/db)
       :db/doc "Name of a shopping item"
       :db/ident :item/name
       :db/valueType :db.type/string
       :db/unique :db.unique/identity
       :db/cardinality :db.cardinality/one
       :db.install/_attribute :db.part/db}
      {:db/id (d/tempid :db.part/db)
       :db/doc "Current amount of a shopping item"
       :db/ident :item/count
       :db/valueType :db.type/long
       :db/cardinality :db.cardinality/one
       :db.install/_attribute :db.part/db}])
    (d/transact
     conn
     [{:db/id #db/id [:db.part/user]
       :db/ident :item/inc-count
       :db/doc "Increments the count of a shopping item"
       :db/fn inc-count}
      {:db/id #db/id [:db.part/user]
       :db/ident :item/dec-count
       :db/doc "Decrements the count of a shopping item"
       :db/fn dec-count}])
    conn))

(defrecord Datomic [uri]
  component/Lifecycle
  (start [this]
    (if (:conn this)
      this
      (assoc this :conn (init uri))))
  (stop [{:keys [conn] :as this}]
    (if conn
      (do
        (.release conn)
        (assoc this :conn nil))
      this)))

(defn datomic-component [{:keys [uri]}]
  (->Datomic uri))
