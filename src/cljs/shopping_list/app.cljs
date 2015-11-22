(ns shopping-list.app
  (:require [reagent.core :as reagent]
            [ajax.core :refer [GET]]
            [shopping-list.state :as state]
            [shopping-list.components.items :as items]
            [shopping-list.components.login :as login]
            [shopping-list.config :as config]))

(defn loading-view []
  [:span ""])

(reset! state/views {:items items/items-view
                     :login login/login-view
                     :loading loading-view})

(defn main-view []
  [(state/get-view)])

(defn render-app [element]
  (let [component (reagent/render [main-view] element)]
    (GET (str config/root "/items")
        {:handler (fn [items]
                    (.log js/console "app started")
                    (state/set-view :items)
                    (items/set-items items))
         :error-handler (fn [{:keys [status status-text failure]}]
                          (condp = status
                            401 (state/set-view :login)
                            (.log js/console (str "app start fail, status: " status ", status-text: " status-text ", failure: " failure))))})
    component))

(defn ^:export main []
  (render-app (.-body js/document)))
