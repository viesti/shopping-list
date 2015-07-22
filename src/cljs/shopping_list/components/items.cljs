(ns shopping-list.components.items
  (:require [reagent.core :as reagent]
            [ajax.core :refer [POST]]
            [shopping-list.state :as state]))

(defonce items (reagent/atom []))
(defonce item-name (reagent/atom ""))
(defonce matches (reagent/atom []))
(defonce selected-match (reagent/atom nil))
(defonce item-control (reagent/atom :add))

(defn set-items [new-items]
  (reset! items new-items))

(defn error-handler [{:keys [status status-text failure]}]
  (.log js/console (str status status-text failure)))

(def handlers {:handler set-items
               :error-handler error-handler})

(defn needed-items []
  [:div.item-box
   [:h1 "Tarvitaan"]
   [:ul (for [{:keys [id name count]} @items
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
    {:class (if (= :add @item-control) "item-control-selected" "item-control-deselected")
     :on-click (fn [_] (reset! item-control :add))}
    "Lisää"]
   [:span " / "]
   [:span
    {:class (if (= :remove @item-control) "item-control-selected" "item-control-deselected")
     :on-click (fn [_] (reset! item-control :remove))}
    "Poista"]
   [:span " vaihtoehtoja"]])

(defn new-selection [dir old-selection]
  (or (when-let [[prev current next] (first (filter (fn [[_ current _]]
                                                      (= old-selection current))
                                                    (partition 3 1 (concat [nil] @matches [nil]))))]
        (if (= dir :up)
          prev next))
      (if (= dir :up)
        (last @matches)
        (first @matches))))

(defn all-items []
  [:div.item-box
   [:h1 "Lisää shoppailuja"]
   [:div
    [:input {:type "text"
             :on-change (fn [event]
                          (let [text (-> event .-target .-value)]
                            (reset! item-name text)
                            (if-not (empty? text)
                              (reset! matches (filter #(re-find (re-pattern (str "(?i)" text)) (:name %)) @items))
                              (reset! matches []))
                            (when-not (seq @matches)
                              (reset! selected-match nil))))
             :on-key-down (fn [event]
                            (when (= 13 (.-keyCode event))
                              (POST "/add" (merge handlers {:params {:item-name (or (:name @selected-match) @item-name)}})))
                            (when (#{40 38} (.-keyCode event))
                              (let [dir (get {40 :down 38 :up} (.-keyCode event))
                                    selection @selected-match]
                                (when (seq @matches)
                                  (swap! selected-match (partial new-selection dir))))))
             :value @item-name}]
    [:button
     {:on-click (fn [_]
                  (POST "/add" (merge handlers {:params {:item-name @item-name}})))}
     "Lisää"]
    [:ul.matches
     {:style {:display (if (seq @matches) "block" "none")}}
     (doall (for [{:keys [id name]} @matches]
              (let [selected-id (:id @selected-match)]
                ^{:key (str id)}
                [:li
                 {:class (if (= selected-id id) "selected" "deselected")
                  :on-click (fn [_]
                              (POST "/add" (merge handlers {:params {:item-name name}})))}
                 name])))]]
   [:ul (doall (for [{:keys [id name]} @items]
                 ^{:key (str id)}
                 [:li
                  [:button {:on-click (fn [_]
                                        (if (= :add @item-control)
                                          (POST "/add" (merge handlers {:params {:item-name name}}))
                                          (POST "/remove" (merge handlers {:params {:id id}}))))}
                   (if (= :add @item-control)
                     "Lisää"
                     "Poista")]
                  [:span name]]))]
   [item-controls]])

(def items-view (with-meta
                  (fn []
                    [:div
                     [needed-items]
                     [all-items]])
                  {:init set-items}))
