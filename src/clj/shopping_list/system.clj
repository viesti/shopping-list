(ns shopping-list.system
  (:require [com.stuartsierra.component :as component]
            [duct.component.endpoint :refer [endpoint-component]]
            [duct.component.handler :refer [handler-component]]
            [duct.middleware.not-found :refer [wrap-not-found]]
            [meta-merge.core :refer [meta-merge]]
            [ring.component.jetty :refer [jetty-server]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [shopping-list.component.datomic :refer [datomic-component]]
            [shopping-list.endpoint.index :refer [index]]
            [shopping-list.endpoint.resources :refer [resources]]
            [shopping-list.endpoint.items :refer [items]]))

(def base-config
  {:app {:middleware [[wrap-not-found :not-found]
                      [wrap-defaults :defaults]]
         :not-found  "Resource Not Found"
         :defaults   api-defaults}})

(defn new-system [config]
  (let [config (meta-merge base-config config)]
    (-> (component/system-map
         :app  (handler-component (:app config))
         :http (jetty-server (:http config))
         :resources (endpoint-component resources)
         :index (endpoint-component index)
         :items (endpoint-component items)
         :datomic (datomic-component (:datomic config)))
        (component/system-using
         {:items [:datomic]
          :http  [:app]
          :app   [:index :resources :items]}))))
