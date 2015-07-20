(ns shopping-list.state
  (:require [reagent.core :as reagent]
            [ajax.core :refer [GET POST]]))

(defonce items (reagent/atom []))
(defonce item-name (reagent/atom ""))
(defonce matches (reagent/atom []))
(defonce selected-match (reagent/atom nil))
(defonce item-control (reagent/atom :add))

(defn update-items [new-items]
  (reset! items new-items))
