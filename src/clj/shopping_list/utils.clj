(ns shopping-list.utils
  (:require [clojure.edn :as edn]))

(defn read-config [filename]
  (try
    (edn/read-string (slurp filename))
    (catch java.io.FileNotFoundException e
      (println (str "Could not find configuration file: " filename)))))
