(ns shopping-list.endpoint.items
  (:import [java.text Collator])
  (:require [compojure.core :refer [GET POST routes wrap-routes]]
            [ring.util.response :refer [response]]
            [ring.middleware.transit :refer [wrap-transit-response wrap-transit-body]]
            [shopping-list.middleware.session-timeout :refer [wrap-session-timeout]]
            [datomic.api :as d]))

(def collator (Collator/getInstance (java.util.Locale/forLanguageTag "fi_FI")))

(defn get-items [db]
  (sort-by :name collator (for [[id name count] (d/q '[:find ?e ?name ?count
                                                       :where
                                                       [?e :item/name ?name]
                                                       [?e :item/count ?count]]
                                                     db)]
                            {:id id
                             :name name
                             :count count})))

(defn items [session-timeout-secs {{conn :conn} :datomic}]
  (-> (routes
       (GET "/items" []
         (response (get-items (d/db conn))))
       (POST "/add" {{item-name :item-name} :body}
         (let [{:keys [db-after]} @(d/transact conn [[:item/inc-count item-name]])]
           (response (get-items db-after))))
       (POST "/buy" {{id :id} :body}
         (let [{:keys [db-after]} @(d/transact conn [[:item/dec-count id]])]
           (response (get-items db-after))))
       (POST "/remove" {{id :id} :body}
         (let [{:keys [db-after]} @(d/transact conn [[:db.fn/retractEntity id]])]
           (response (get-items db-after)))))
      (wrap-routes wrap-transit-response)
      (wrap-routes wrap-transit-body)
      (wrap-routes wrap-session-timeout session-timeout-secs)))
