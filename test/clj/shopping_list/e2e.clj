(ns shopping-list.e2e
  (:import (java.io ByteArrayOutputStream))
  (:require [clojure.test :refer :all]
            [cognitect.transit :as transit]
            [com.stuartsierra.component :as component]
            [datomic.api :as d]
            [peridot.core :as p]
            [shopping-list.system :as system]
            [buddy.hashers :as hashers]
            [doo.core :as doo]
            [cljs.build.api :as cljs]
            [ring.middleware.cors :refer [wrap-cors]]))

(def session-key-file (atom nil))
(def datomic-uri (str "datomic:mem://" (d/squuid)))

(use-fixtures :each (fn [f]
                      (reset! session-key-file (java.io.File/createTempFile "sessionkey" nil))
                      (with-open [out (clojure.java.io/output-stream @session-key-file)]
                        (.write out (byte-array 16) 0 16))
                      (let [system (-> (system/new-system {:http {:port 3333
                                                                  :session-key-file (.getAbsolutePath @session-key-file)
                                                                  :session-timeout-secs 30000
                                                                  :cookie-max-age 3600}
                                                           :datomic {:uri datomic-uri}
                                                           :app {:middleware [(fn [handler]
                                                                                (wrap-cors handler
                                                                                           :access-control-allow-origin [#".*"]
                                                                                           :access-control-allow-methods [:get :put :post :delete]))]}})
                                       (assoc :nrepl {}) ;; Disable nrepl server
                                       component/start)]
                        (d/transact (d/connect datomic-uri)
                                    [{:db/id #db/id[:db.part/user]
                                      :user/username "foo"
                                      :user/password (hashers/encrypt "bar" {:algorithm :bcrypt+sha512})}])
                        (f)
                        (component/stop system))
                      (.delete @session-key-file)
                      (d/delete-database datomic-uri)))

(deftest suite
  (let [doo-opts {:paths {}}
        compiler-opts {:main 'shopping-list.e2e-runner
                       :output-to "out/test.js"
                       :output-dir "out"
                       :asset-path "out"
                       :optimizations :none}]
    (cljs/build (apply cljs/inputs ["src/cljs" "test/cljs" "test/cljs-app-config"]) compiler-opts)
    (doo/run-script :phantom compiler-opts doo-opts)))
