(ns shopping-list.unit-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [shopping-list.simple-test]))

(doo-tests 'shopping-list.simple-test)
