(ns shopping-list.components.login
  (:require [reagent.core :as reagent]
            [ajax.core :refer [POST]]
            [shopping-list.state :as state]))

(defn login [username password on-login]
  (POST "/login"
      {:params {:username @username
                :password @password}
       :handler (fn [items]
                  (reset! username nil)
                  (reset! password nil)
                  (state/set-view :items items)
                  (on-login nil))
       :error-handler (fn [{:keys [status status-text failure]}]
                        (.log js/console (str status status-text failure))
                        (on-login failure))}))

(defn login-view []
  (let [username (reagent/atom nil)
        password (reagent/atom nil)
        error (reagent/atom nil)
        on-login #(reset! error %1)]
    (fn []
      [:div.login
       [:h1.title "Kauppalista"]
       [:div.controls
        [:input {:type "text"
                 :placeholder "tunnus"
                 :class (when @error "error")
                 :on-change
                 (fn [event]
                   (reset! error nil)
                   (reset! username (-> event .-target .-value)))}]
        [:input {:type "password"
                 :placeholder "salasana"
                 :class (when @error "error")
                 :on-key-down
                 (fn [event]
                   (when (= 13 (.-keyCode event))
                     (login username password on-login)))
                 :on-change
                 (fn [event]
                   (reset! error nil)
                   (reset! password (-> event .-target .-value)))}]
        [:button
         {:on-click
          #(login username password on-login)}
         "Kirjaudu"]]])))
