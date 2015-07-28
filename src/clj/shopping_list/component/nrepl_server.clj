(ns shopping-list.component.nrepl-server
  (:require [clojure.tools.nrepl.server :refer [start-server stop-server]]
            [com.stuartsierra.component :as component]))

(defrecord NreplServer [port]
  component/Lifecycle
  (start [this]
    (if (:server this)
      this
      (let [component (assoc this :server (start-server :port port))]
        (println "Started nrepl server in port:" port)
        component)))
  (stop [this]
    (if-let [server (:server this)]
      (do
        (stop-server server)
        (println "Stopped nrepl server")
        (assoc this :server nil))
      this)))

(defn nrepl-server-component [port]
  (->NreplServer port))
