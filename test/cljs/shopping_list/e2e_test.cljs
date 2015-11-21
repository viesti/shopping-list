(ns shopping-list.e2e-test
  (:require [cljs.test :refer-macros [async deftest is testing use-fixtures]]
            [goog.dom :as gdom]
            [reagent.core :as reagent]
            [shopping-list.app :as app]
            [shopping-list.state :as state]
            [cljs-react-test.simulate :as sim]
            [cljs-react-test.utils :as tu]
            [dommy.core :as dommy :refer-macros [sel1]]))

(def container (atom nil))

(use-fixtures :each
  {:before #(async done
              (reset! container (tu/new-container!))
              (done))
   :after #(tu/unmount! @container)})

(deftest login
  (testing "First view is loading"
    (is (= :loading @state/current-view)))
  (async done
    (app/render-app @container)
    (.setTimeout js/window
                 (fn []
                   (testing "then it is login"
                     (is (= :login @state/current-view)))
                   (let [username (sel1 @container :#username)
                         password (sel1 @container :#password)
                         login (sel1 @container :#login)]
                     (sim/change username {:target {:value "foo"}})
                     (sim/change password {:target {:value "bar"}})
                     (sim/click login nil))
                   (.setTimeout js/window
                                (fn []
                                  (testing "after login, items are displayed"
                                    (is (= :login @state/current-view)))
                                  (done))
                                3000))
                 1000)))
