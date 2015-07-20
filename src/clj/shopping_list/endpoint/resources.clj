(ns shopping-list.endpoint.resources
  (:require [compojure.core :refer [GET routes]]
            [compojure.route :as route]))

(defn resources [_]
  (routes
   (route/resources "/")))
