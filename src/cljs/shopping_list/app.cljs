(ns shopping-list.app
  (:require [reagent.core :as reagent]
            [ajax.core :refer [GET POST]]
            [shopping-list.state :as state]))

(defn error-handler [{:keys [status status-text failure]}]
  (.log js/console (str status status-text failure)))

(def handlers {:handler state/update-items
               :error-handler error-handler})

(defn needed-items []
  [:div.item-box
   [:h1 "Tarvitaan"]
   [:ul (for [{:keys [id name count]} @state/items
              :when (pos? count)]
          ^{:key (str id)}
          [:li [:div
                [:button.icon-ok {:on-click (fn [_]
                                              (POST "/buy" (merge handlers {:params {:id id}})))}]
                [:span (str (if (> count 1)
                              (str count " ")
                              "") name)]]])]])

(defn item-controls []
  [:div
   [:span
    {:class (if (= :add @state/item-control) "item-control-selected" "item-control-deselected")
     :on-click (fn [_] (reset! state/item-control :add))}
    "Lisää"]
   [:span " / "]
   [:span
    {:class (if (= :remove @state/item-control) "item-control-selected" "item-control-deselected")
     :on-click (fn [_] (reset! state/item-control :remove))}
    "Poista"]
   [:span " vaihtoehtoja"]])

(defn new-selection [dir old-selection]
  (or (when-let [[prev current next] (first (filter (fn [[_ current _]]
                                                      (= old-selection current))
                                                    (partition 3 1 (concat [nil] @state/matches [nil]))))]
        (if (= dir :up)
          prev next))
      (if (= dir :up)
        (last @state/matches)
        (first @state/matches))))

(defn all-items []
  [:div.item-box
   [:h1 "Lisää shoppailuja"]
   [:div
    [:input {:type "text"
             :on-change (fn [event]
                          (let [text (-> event .-target .-value)]
                            (reset! state/item-name text)
                            (if-not (empty? text)
                              (reset! state/matches (filter #(re-find (re-pattern (str "(?i)" text)) (:name %)) @state/items))
                              (reset! state/matches []))
                            (when-not (seq @state/matches)
                              (reset! state/selected-match nil))))
             :on-key-down (fn [event]
                            (when (= 13 (.-keyCode event))
                              (POST "/add" (merge handlers {:params {:item-name (or (:name @state/selected-match) @state/item-name)}})))
                            (when (#{40 38} (.-keyCode event))
                              (let [dir (get {40 :down 38 :up} (.-keyCode event))
                                    selection @state/selected-match]
                                (when (seq @state/matches)
                                  (swap! state/selected-match (partial new-selection dir))))))
             :value @state/item-name}]
    [:button
     {:on-click (fn [_]
                  (POST "/add" (merge handlers {:params {:item-name @state/item-name}})))}
     "Lisää"]
    [:ul.matches
     {:style {:display (if (seq @state/matches) "block" "none")}}
     (doall (for [{:keys [id name]} @state/matches]
              (let [selected-id (:id @state/selected-match)]
                ^{:key (str id)}
                [:li
                 {:class (if (= selected-id id) "selected" "deselected")
                  :on-click (fn [_]
                              (POST "/add" (merge handlers {:params {:item-name name}})))}
                 name])))]]
   [:ul (doall (for [{:keys [id name]} @state/items]
                 ^{:key (str id)}
                 [:li
                  [:button {:on-click (fn [_]
                                        (if (= :add @state/item-control)
                                          (POST "/add" (merge handlers {:params {:item-name name}}))
                                          (POST "/remove" (merge handlers {:params {:id id}}))))}
                   (if (= :add @state/item-control)
                     "Lisää"
                     "Poista")]
                  [:span name]]))]
   [item-controls]])

(defn main-view []
  [:div
   [needed-items]
   [all-items]])

(defn ^:export main []
  (reagent/render [main-view]
    (.-body js/document))
  (GET "/items" handlers))
