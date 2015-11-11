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
            [shopping-list.system :as system]
            [buddy.hashers :as hashers]))

(defn- write [x t opts]
  (let [baos (ByteArrayOutputStream.)
        w    (transit/writer baos t opts)
        _    (transit/write w x)
        ret  (.toString baos)]
    (.reset baos)
    ret))

(def session-key-file (atom nil))
(def datomic-uri (str "datomic:mem://" (d/squuid)))

(use-fixtures :each (fn [f]
                      (reset! session-key-file (java.io.File/createTempFile "sessionkey" nil))
                      (with-open [out (clojure.java.io/output-stream @session-key-file)]
                        (.write out (byte-array 16) 0 16))
                      (f)
                      (.delete @session-key-file)
                      (d/delete-database datomic-uri)))

(deftest session-timeout-incremented
  (let [timeout-counter (atom 0)]
    (with-redefs [shopping-list.middleware.session-timeout/wrap-session-timeout (fn [handler session-timeout-secs]
                                                                                  (fn [request]
                                                                                    (swap! timeout-counter inc)
                                                                                    (handler request)))]
      (let [system (-> (system/new-system {:http {:port 3000
                                                  :session-key-file (.getAbsolutePath @session-key-file)
                                                  :session-timeout-secs 30000
                                                  :cookie-max-age 3600}
                                           :datomic {:uri datomic-uri}})
                       (assoc :http {}) ;; Mock out Jetty
                       (assoc :nrepl {}) ;; Disable nrepl server
                       component/start)]
        (d/transact (d/connect datomic-uri)
                    [{:db/id #db/id[:db.part/user]
                      :user/username "foo"
                      :user/password (hashers/encrypt "bar" {:algorithm :bcrypt+sha512})}])
        (-> (p/session (:handler (:app system)))
            (p/content-type "application/transit+json; charset=utf-8")
            (p/request "/login"
                       :request-method :post
                       :body (write {:username "foo" :password "bar"} :json {}))
            ((fn [state]
               state))
            (p/request "/somenonexistingurl")
            ((fn [x]
               (is (zero? @timeout-counter))
               x))
            (p/request "/items")
            ((fn [x]
               (is (= 1 @timeout-counter))
               x)))))))

(deftest listing-items-without-session
  (let [system (-> (system/new-system {:http {:port 3000
                                              :session-key-file (.getAbsolutePath @session-key-file)
                                              :session-timeout-secs 30000
                                              :cookie-max-age 3600}
                                       :datomic {:uri datomic-uri}})
                   (assoc :http {}) ;; Mock out Jetty
                   (assoc :nrepl {}) ;; Disable nrepl server
                   component/start)]
    (d/transact (d/connect datomic-uri)
                [{:db/id #db/id[:db.part/user]
                  :user/username "foo"
                  :user/password (hashers/encrypt "bar" {:algorithm :bcrypt+sha512})}])
    (-> (p/session (:handler (:app system)))
        (p/content-type "application/transit+json; charset=utf-8")
        (p/request "/items")
        ((fn [state]
           (is (= 401 (get-in state [:response :status])))
           state))
        (p/request "/login"
                   :request-method :post
                   :body (write {:username "foo" :password "barbar"} :json {}))
        (p/request "/items")
        ((fn [state]
           (is (= 401 (get-in state [:response :status])))
           state))
        (p/request "/login"
                   :request-method :post
                   :body (write {:username "foo" :password "bar"} :json {}))
        (p/request "/items")
        ((fn [state]
           (is (= 200 (get-in state [:response :status])))
           state)))))
