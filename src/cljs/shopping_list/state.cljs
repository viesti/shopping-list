(ns shopping-list.state
  (:require [reagent.core :as reagent]))

(defonce views (atom {}))

(defonce current-view (reagent/atom :login))

(defn get-view []
  (get @views @current-view))

(defn set-view
  ([view]
   (reset! current-view view))
  ([view data]
   (reset! current-view view)
   (when-let [init (:init (meta (get-view)))]
     (init data))))

