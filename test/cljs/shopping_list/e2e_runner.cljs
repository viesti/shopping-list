(ns shopping-list.e2e-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [shopping-list.e2e-test]))

(doo-tests 'shopping-list.e2e-test)
