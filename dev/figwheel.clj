(ns figwheel
  (:require [com.stuartsierra.component :as component]
            [figwheel-sidecar.core :as core]
            [figwheel-sidecar.repl-api :as repl]))

(defrecord Figwheel [config]
  component/Lifecycle
  (start [this]
    (if (:started this)
      this
      (let [{:keys [source-paths compiler]} (->> "project.clj" slurp read-string (drop 3) (apply hash-map) :cljsbuild :builds :dev)]
        (repl/start-figwheel! {:figwheel-options {:nrepl-port 7889}
                               :build-ids ["dev"]
                               :all-builds [{:id "dev"
                                             :source-paths source-paths
                                             :compiler compiler}]})
        (repl/start-autobuild)
        (assoc this :started true))))
  (stop [this]
    (assoc this :started nil)))

(defn figwheel-component [config]
  (->Figwheel config))
