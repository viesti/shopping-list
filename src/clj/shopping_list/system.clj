(ns shopping-list.system
  (:require [com.stuartsierra.component :as component]
            [duct.component.endpoint :refer [endpoint-component]]
            [duct.component.handler :refer [handler-component]]
            [duct.middleware.not-found :refer [wrap-not-found]]
            [meta-merge.core :refer [meta-merge]]
            [ring.component.jetty :refer [jetty-server]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.session.cookie :as cookie]
            [shopping-list.component.datomic :refer [datomic-component]]
            [shopping-list.component.nrepl-server :refer [nrepl-server-component]]
            [shopping-list.endpoint.index :refer [index]]
            [shopping-list.endpoint.resources :refer [resources]]
            [shopping-list.endpoint.items :refer [items]]
            [shopping-list.endpoint.authentication :refer [authentication]]))

(defn base-config [session-key max-age]
  {:app {:middleware [[wrap-not-found :not-found]
                      [wrap-defaults :defaults]]
         :not-found  "Resource Not Found"
         :defaults   (meta-merge api-defaults {:session {:store (cookie/cookie-store {:key session-key})
                                                         :cookie-attrs {:http-only true
                                                                        :max-age max-age}
                                                         :flash true}})}})

(defn read-session-key [path]
  (with-open [in (clojure.java.io/input-stream path)]
    (let [bs (byte-array 16)]
      (.read in bs)
      bs)))

(defn new-system [config]
  (let [session-key (read-session-key (-> config :http :session-key-file))
        dev-components (:dev-components config)
        config (meta-merge (base-config session-key (-> config :http :cookie-max-age)) config)]
    (-> (component/map->SystemMap
         (conj {
                ;; services
                :app  (handler-component (:app config))
                :http (jetty-server (:http config))
                :datomic (datomic-component (:datomic config))
                :nrepl (nrepl-server-component (:nrepl config))
                ;; endpoints
                :resources (endpoint-component resources)
                :index (endpoint-component index)
                :items (endpoint-component (partial items (-> config :http :session-timeout-secs)))
                :login (endpoint-component authentication)}
               dev-components))
        (component/system-using
         {:items [:datomic]
          :login [:datomic]
          :http  [:app :nrepl]
          :app   [:login :index :resources :items]}))))
