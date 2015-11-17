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
  (.log js/console (str status " " status-text " " failure))
  (state/set-view :login))

(def handlers {:handler set-items
               :error-handler error-handler})

(defn reset-inputs []
  (reset! item-name "")
  (reset! matches [])
  (reset! selected-match nil))

(defn set-items-and-reset-inputs [items]
  (set-items items)
  (reset-inputs))

(defn needed-items []
  [:div.item-box
   [:h1 "Tarvitaan"]
   [:ul (for [{:keys [id name count]} @items
              :when (pos? count)]
          ^{:key (str id)}
          [:li [:div
                [:button.icon-ok {:on-click
                                  (fn [_]
                                    (POST "/buy" (merge handlers {:params {:id id}})))}]
                [:span.item-name (str (if (> count 1)
                                        (str count " ")
                                        "") name)]]])]])

(defn item-controls []
  [:div.item-controls
   [:input
    {:type "radio"
     :value "item-control-type"
     :checked (= :add @item-control)
     :on-change
     (fn [_] (reset! item-control :add))}
    "Lisää"]
   [:span " / "]
   [:input
    {:type "radio"
     :value "item-control-type"
     :checked (= :remove @item-control)
     :on-change
     (fn [_] (reset! item-control :remove))}
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

(defn optimistic-inc [name]
  (swap! items (partial map
                        (fn [item]
                          (if (= name (:name item))
                            (update item :count inc)
                            item))))
  (reset-inputs))

(defn matches-list []
  [:ul.matches
   {:class (if (seq @matches) "matches-visible" "matches-invisible")}
   (doall (for [{:keys [id name]} @matches]
            (let [selected-id (:id @selected-match)]
              ^{:key (str id)}
              [:li
               {:class (if (= selected-id id) "match-selected" "match-deselected")
                :on-click
                (fn [_]
                  (optimistic-inc name)
                  (POST "/add" (merge handlers {:params {:item-name name}
                                                :handler set-items-and-reset-inputs})))}
               name])))])

(defn optimistic-add [name]
  (swap! items
         (comp (partial sort-by :name) conj)
         {:name name
          :count 1
          :id "some-new-item"})
  (reset-inputs))

(defn optimistic-add-or-inc [name]
  (if (seq (filter #(= name (:name %)) @items))
    (optimistic-inc name)
    (optimistic-add name)))

(defn new-item []
  [:div.new-item
   [:input
    {:type "text"
     :on-blur
     (fn [_] (reset! matches []))
     :on-change
     (fn [event]
       (let [text (-> event .-target .-value)]
         (reset! item-name text)
         (if-not (empty? text)
           (reset! matches (filter #(re-find (re-pattern (str "(?i)" text)) (:name %)) @items))
           (reset! matches []))
         (when-not (seq @matches)
           (reset! selected-match nil))))
     :on-key-down
     (fn [event]
       (when (and (not (empty? @item-name)) (= 13 (.-keyCode event)))
         (let [name (or (:name @selected-match) @item-name)]
           (optimistic-add-or-inc name)
           (POST "/add" (merge handlers {:params {:item-name name}
                                         :handler set-items-and-reset-inputs}))))
       (when (#{40 38} (.-keyCode event))
         (let [dir (get {40 :down 38 :up} (.-keyCode event))
               selection @selected-match]
           (when (seq @matches)
             (swap! selected-match (partial new-selection dir))))))
     :value @item-name}]
   [:button
    {:on-click
     (fn [_]
       (POST "/add" (merge handlers {:params {:item-name @item-name}
                                     :handler set-items-and-reset-inputs})))}
    "Lisää"]])

(defn all-items []
  [:div.item-box
   [:h1 "Lisää shoppailuja"]
   [new-item]
   [matches-list]
   [:ul (doall (for [{:keys [id name]} @items]
                 ^{:key (str id)}
                 [:li
                  [:button.item-control {:on-click
                                         (fn [_]
                                           (if (= :add @item-control)
                                             (POST "/add" (merge handlers {:params {:item-name name}}))
                                             (POST "/remove" (merge handlers {:params {:id id}}))))}
                   (if (= :add @item-control)
                     "Lisää"
                     "Poista")]
                  [:span.item-name name]]))]
   [item-controls]])

(defn logout []
  [:button.logout {:on-click
                   (fn [event]
                     (POST "/logout"
                         {:handler
                          (fn [msg]
                            (.log js/console msg)
                            (state/set-view :login))
                          :error-handler error-handler}))}
   "Kirjaudu pois"])

(defn view []
  [:div.content
   [:div.items
    [needed-items]
    [all-items]]
   [logout]])

(def items-view (with-meta
                  view
                  {:init set-items}))
