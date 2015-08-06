(defproject shopping-list "0.1.0-SNAPSHOT"
  :description "Shopping list"
  :url "http://kauppalista.tiuhti.net"
  :min-lein-version "2.5.0"
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :username :env
                                   :password :env}}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.stuartsierra/component "0.2.3"]
                 [compojure "1.4.0"]
                 [duct "0.1.2"]
                 [meta-merge "0.1.1"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-jetty-component "0.2.2"]
                 [ring-transit "0.1.3"]
                 [prismatic/plumbing "0.4.4"]
                 [com.datomic/datomic-pro "0.9.5201" :exclusions [joda-time]]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]
                 [buddy/buddy-hashers "0.6.0"]
                 [org.clojure/tools.nrepl "0.2.10"]

                 [org.clojure/clojurescript "0.0-3308"]
                 [cljs-ajax "0.3.13"]
                 [reagent "0.5.0"]]
  :plugins [[lein-gen "0.2.2"]
            [lein-figwheel "0.3.7"]
            [lein-cljsbuild "1.0.6"]
            [com.jakemccrary/lein-test-refresh "0.10.0"]]
  :generators [[duct/generators "0.1.2"]]
  :duct {:ns-prefix shopping-list}
  :main ^:skip-aot shopping-list.main
  :uberjar-name "shopping-list.jar"
  :aliases {"gen"   ["generate"]
            "setup" ["do" ["generate" "locals"]]}
  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj" "test/cljc"]
  :cljsbuild {:builds {:dev {:source-paths ["src/cljs"]
                             :figwheel {:on-jsload "shopping-list.app/main"}
                             :compiler {:main shopping-list.app
                                        :output-to "resources/public/js/app.js"
                                        :output-dir "resources/public/js/out"
                                        :asset-path "js/out"
                                        :optimizations :none
                                        :pretty-print true}}
                       :prod {:source-paths ["src/cljs"]
                              :compiler {:output-to "resources/public/js/app.js"
                                         :optimizations :advanced}}}}
  :figwheel {:nrepl-port 7889
             :open-file-command "myfile-opener"
             :css-dirs ["resources/public/css"]}
  :clean-targets ^{:protect false} ["resources/public/js/app.js"
                                    "resources/public/js/out"
                                    :target-path]
  :target-path "target/%s/"
  :profiles
  {:dev  [:project/dev  :profiles/dev]
   :test [:project/test :profiles/test]
   :uberjar {:aot :all
             :prep-tasks ^:replace ["clean"
                                    ["cljsbuild" "once" "prod"]
                                    "javac"
                                    "compile"]}
   :profiles/dev  {}
   :profiles/test {}
   :project/dev   {:source-paths ["dev"]
                   :repl-options {:init-ns user}
                   :dependencies [[reloaded.repl "0.1.0"]
                                  [org.clojure/tools.namespace "0.2.11"]
                                  [org.clojure/tools.nrepl "0.2.10"]
                                  [peridot "0.4.0"]]}
   :project/test  {}})
