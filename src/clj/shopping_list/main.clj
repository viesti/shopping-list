(ns shopping-list.main
  (:gen-class)
  (:require [com.stuartsierra.component :as component]
            [duct.middleware.errors :refer [wrap-hide-errors]]
            [meta-merge.core :refer [meta-merge]]
            [shopping-list.system :refer [new-system]]
            [shopping-list.utils :as utils]))

(def app-system (atom nil))

(def prod-config
  {:app {:middleware     [[wrap-hide-errors :internal-error]]
         :internal-error "Internal Server Error"}})

(defn -main [& args]
  (let [config (utils/read-config (or (first args) "config.edn"))]
    (let [system (new-system (meta-merge prod-config config))]
      (println "Starting shopping list on port" (-> system :http :port))
      (try
        (let [started-system (component/start system)]
          (reset! app-system started-system)
          (.addShutdownHook (Runtime/getRuntime) (java.lang.Thread. #(component/stop started-system)))
          (println "Running, yay!"))
        (catch Throwable t
          (println "Error while starting application:")
          (println t)
          (System/exit 1))))))
