(ns shopping-list.endpoint.authentication
  (:require [compojure.core :refer [POST routes wrap-routes]]
            [ring.util.response :refer [response status]]
            [ring.middleware.transit :refer [wrap-transit-response wrap-transit-body]]
            [ring.middleware.session :as session]
            [compojure.route :as route]
            [datomic.api :as d]
            [shopping-list.endpoint.items :refer [get-items]]
            [buddy.hashers :as hashers]))

(defn authentication [{{conn :conn} :datomic}]
  (-> (routes
       (POST "/login" request
         (let [{:keys [body session]} request
               {:keys [username password]} body]
           (if (hashers/check password
                              (ffirst (d/q '[:find ?password
                                             :in $ ?username
                                             :where
                                             [?e :user/username ?username]
                                             [?e :user/password ?password]]
                                           (d/db conn)
                                           username)))
             (-> (response (get-items (d/db conn)))
                 (update :session assoc :identity true)
                 (update :session assoc :last-activity (System/currentTimeMillis)))
             (-> (response "Unauthorized")
                 (status 401)))))
       (POST "/logout" {session :session}
         (-> (response "Goodbye")
             (update :session assoc :identity false)
             (update :session assoc :last-activity 0))))
      (wrap-routes wrap-transit-response)
      (wrap-routes wrap-transit-body)))
