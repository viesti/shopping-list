(ns shopping-list.endpoint.index
  (:require [compojure.core :refer [GET routes]]
            [ring.util.response :refer [resource-response content-type]]
            [compojure.route :as route]))

(defn index [_]
  (routes
   (GET "/" []
     (content-type (resource-response "index.html" {:root "public"})
                   "text/html"))))
