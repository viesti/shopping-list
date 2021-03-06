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
            [shopping-list.utils :as utils]
            [figwheel-sidecar.repl-api :as fr]
            [datomic.api :as d]
            [buddy.hashers :as hashers]
            [eftest.runner :as eftest]))

(ns-unmap *ns* 'test)

(defn test []
  (eftest/run-tests (eftest/find-tests "test/clj") {:multithread? false}))

(def dev-config
  {:app {:middleware [wrap-stacktrace]}})

(reloaded.repl/set-init! #(system/new-system (meta-merge dev-config
                                                         (utils/read-config "config.edn")
                                                         (when (.exists (io/file "config-local.edn"))
                                                           (utils/read-config "config-local.edn")))))

(defn insert-test-user []
  (d/transact (d/connect (-> system :datomic :uri))
              [{:db/id #db/id[:db.part/user]
                                           :user/username "foo"
                :user/password (hashers/encrypt "bar" {:algorithm :bcrypt+sha512})}]))
