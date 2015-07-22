(ns shopping-list.endpoint.login
  (:require [compojure.core :refer [POST routes wrap-routes]]
            [ring.util.response :refer [response]]
            [ring.middleware.transit :refer [wrap-transit-response wrap-transit-body]]
            [compojure.route :as route]
            [datomic.api :as d]
            [shopping-list.endpoint.items :refer [get-items]]))

(defn login [{{conn :conn} :datomic}]
  (-> (routes
       (POST "/login" []
         (response (get-items (d/db conn)))))
      (wrap-routes wrap-transit-response)
      (wrap-routes wrap-transit-body)))
