(ns shopping-list.component.nrepl-server
  (:require [clojure.tools.nrepl.server :refer [start-server stop-server]]
            [com.stuartsierra.component :as component]))

(defrecord NreplServer [host port]
  component/Lifecycle
  (start [this]
    (if (:server this)
      this
      (let [component (assoc this :server (start-server :bind host :port port))]
        (println (str "Started nrepl server at " host ":" port))
        component)))
  (stop [this]
    (if-let [server (:server this)]
      (do
        (stop-server server)
        (println "Stopped nrepl server")
        (assoc this :server nil))
      this)))

(defn nrepl-server-component [{:keys [host port]}]
  (->NreplServer host port))
