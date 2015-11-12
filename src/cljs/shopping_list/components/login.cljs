(ns shopping-list.components.login
  (:require [reagent.core :as reagent]
            [ajax.core :refer [POST]]
            [shopping-list.state :as state]))

(defn login [username password]
  (POST "/login"
      {:params {:username @username
                :password @password}
       :handler (fn [items]
                  (reset! username nil)
                  (reset! password nil)
                  (state/set-view :items items))
       :error-handler (fn [{:keys [status status-text failure]}]
                        (.log js/console (str status status-text failure)))}))

(defn login-view []
  (let [username (reagent/atom nil)
        password (reagent/atom nil)]
    (fn []
      [:div.login
       [:div.labels
        [:span "Tunnus"]
        [:span "Salasana"]
        [:span]]
       [:div.controls
        [:input {:type "text"
                 :on-change
                 (fn [event]
                   (reset! username (-> event .-target .-value)))}]
        [:input {:type "password"
                 :on-key-down
                 (fn [event]
                   (when (= 13 (.-keyCode event))
                     (login username password)))
                 :on-change
                 (fn [event]
                   (reset! password (-> event .-target .-value)))}]
        [:button
         {:on-click
          #(login username password)}
         "Kirjaudu"]]])))
