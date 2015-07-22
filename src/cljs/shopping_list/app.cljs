(ns shopping-list.app
  (:require [reagent.core :as reagent]
            [ajax.core :refer [GET]]
            [shopping-list.state :as state]
            [shopping-list.components.items :as items]
            [shopping-list.components.login :as login]))

(reset! state/views {:items items/items-view
                     :login login/login-view})

(defn main-view []
  [(state/get-view)])

(defn ^:export main []
  (reagent/render [main-view]
    (.-body js/document))
  (GET "/items"
      {:handler (fn [items]
                  (state/set-view :items)
                  (items/set-items items))
       :error-handler (fn [{:keys [status status-text failure]}]
                        (state/set-view :login))}))
