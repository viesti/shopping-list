(ns shopping-list.endpoint.index
  (:require [compojure.core :refer [GET routes]]
            [ring.util.response :as response]
            [compojure.route :as route]))

(defn index [_]
  (routes
   (GET "/" []
     (response/redirect "/index.html"))))
