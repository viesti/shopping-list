(ns figwheel
  (:require [com.stuartsierra.component :as component]
            [figwheel-sidecar.core :as core]
            [figwheel-sidecar.repl-api :as repl]))

(defrecord Figwheel [config]
  component/Lifecycle
  (start [this]
    (if (:started this)
      this
      (do
        (repl/start-figwheel! {:figwheel-options {:nrepl-port 7889}
                               :build-ids ["dev"]
                               :all-builds [{:id "dev"
                                             :source-paths ["src/cljs"]
                                             :compiler {:main "shopping-list.app"
                                                        :output-to "resources/public/js/app.js"
                                                        :output-dir "resources/public/js/out"
                                                        :asset-path "js/out"
                                                        :optimizations :none
                                                        :pretty-print true}}]})
        (repl/start-autobuild)
        (assoc this :started true))))
  (stop [this]
    (assoc this :started nil)))

(defn figwheel-component [config]
  (->Figwheel config))
