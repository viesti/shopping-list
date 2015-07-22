(ns shopping-list.endpoint.index
  (:require [compojure.core :refer [GET routes]]
            [ring.util.response :refer [redirect]]
            [compojure.route :as route]))

(defn index [_]
  (routes
   (GET "/" []
     (redirect "/index.html"))))
