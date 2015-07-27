(ns shopping-list.items-test
  (:import (java.io ByteArrayOutputStream))
  (:require [clojure.test :refer :all]
            [cognitect.transit :as transit]
            [com.stuartsierra.component :as component]
            [datomic.api :as d]
            [peridot.core :as p]
            [shopping-list.component.datomic :as datomic]
            [shopping-list.endpoint.authentication :refer [authentication]]
            [shopping-list.endpoint.items :refer [items]]
            [shopping-list.system :as system]))

(defn- write [x t opts]
  (let [baos (ByteArrayOutputStream.)
        w    (transit/writer baos t opts)
        _    (transit/write w x)
        ret  (.toString baos)]
    (.reset baos)
    ret))

(def session-key-file (atom nil))

(defn with-session-key [f]
  (reset! session-key-file (java.io.File/createTempFile "sessionkey" nil))
  (with-open [out (clojure.java.io/output-stream @session-key-file)]
    (.write out (byte-array 16) 0 16))
  (f)
  (.delete @session-key-file))

(use-fixtures :each with-session-key)

(deftest session-timeout-incremented
  (let [timeout-counter (atom 0)]
    (with-redefs [shopping-list.middleware.session-timeout/wrap-session-timeout (fn [handler]
                                                                                  (fn [request]
                                                                                    (swap! timeout-counter inc)
                                                                                    (handler request)))]
      (let [uri (str "datomic:mem://" (d/squuid))
            system (-> (system/new-system {:http {:port 3000
                                                  :session-key-file (.getAbsolutePath @session-key-file)}
                                           :datomic {:uri uri}})
                       (assoc :http {}) ;; Mock out Jetty
                       component/start)]
        (-> (p/session (:handler (:app system)))
            (p/content-type "application/transit+json; charset=utf-8")
            (p/request "/login"
                       :request-method :post
                       :body (write {:username "foo" :password "bar"} :json {}))
            (p/request "/somenonexistingurl")
            ((fn [x]
               (is (zero? @timeout-counter))
               x))
            (p/request "/items")
            ((fn [x]
               (is (= 1 @timeout-counter))
               x)))))))
