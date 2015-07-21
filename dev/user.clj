(ns user
  (:require [clojure.repl :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [meta-merge.core :refer [meta-merge]]
            [reloaded.repl :refer [system init start stop go reset]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [shopping-list.system :as system]
            [shopping-list.utils :as utils]))

(def dev-config
  {:app {:middleware [wrap-stacktrace]}})

(reloaded.repl/set-init! #(system/new-system (meta-merge dev-config
                                                         (utils/read-config "config.edn")
                                                         (if (.exists (io/file "config-local.edn"))
                                                           (utils/read-config "config-local.edn")
                                                           {}))))
