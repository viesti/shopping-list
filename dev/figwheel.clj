(ns figwheel
  (:require [com.stuartsierra.component :as component]
            [figwheel-sidecar.core :as core]
            [figwheel-sidecar.repl-api :as repl]))

(defrecord Figwheel [config]
  component/Lifecycle
  (start [this]
    (if (:server this)
      this
      (assoc this :server (core/start-server {:output-to "resources/public/js/app.js"
                                              :output-dir "resources/public/js/out"}))))
  (stop [{:keys [server] :as this}]
    ((:http-server server))
    (assoc this :server nil)))

(defn figwheel-component [config]
  (->Figwheel config))
