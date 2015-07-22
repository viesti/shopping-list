(ns shopping-list.endpoint.items
  (:require [compojure.core :refer [GET POST routes wrap-routes]]
            [ring.util.response :refer [response]]
            [ring.middleware.transit :refer [wrap-transit-response wrap-transit-body]]
            [datomic.api :as d]))

(defn get-items [db]
  (sort-by :name (for [[id name count] (d/q '[:find ?e ?name ?count
                                              :where
                                              [?e :item/name ?name]
                                              [?e :item/count ?count]]
                                            db)]
                   {:id id
                    :name name
                    :count count})))

(defn items [{{conn :conn} :datomic}]
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
      (wrap-routes wrap-transit-body)))
