(defproject shopping-list "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.stuartsierra/component "0.2.3"]
                 [compojure "1.4.0"]
                 [duct "0.1.2"]
                 [environ "1.0.0"]
                 [meta-merge "0.1.1"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-jetty-component "0.2.2"]
                 [ring-transit "0.1.3"]
                 [prismatic/plumbing "0.4.4"]
                 [com.datomic/datomic-pro "0.9.5201" :exclusions [joda-time]]

                 [org.clojure/clojurescript "0.0-3308"]
                 [cljs-ajax "0.3.13"]
                 [reagent "0.5.0"]]
  :plugins [[lein-environ "1.0.0"]
            [lein-gen "0.2.2"]
            [lein-figwheel "0.3.7"]
            [lein-cljsbuild "1.0.6"]]
  :generators [[duct/generators "0.1.2"]]
  :duct {:ns-prefix shopping-list}
  :main ^:skip-aot shopping-list.main
  :aliases {"gen"   ["generate"]
            "setup" ["do" ["generate" "locals"]]}
  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj" "test/cljc"]
  :cljsbuild {:builds {:dev {:source-paths ["src/cljs"]
                             :figwheel {:on-jsload "shopping-list.app/main"}
                             :compiler {:output-to "resources/public/js/app.js"
                                        :output-dir "resources/public/js/out"
                                        :asset-path "js/out"
                                        :optimizations :none
                                        :pretty-print true}}}}
  :figwheel {:nrepl-port 7889
             :open-file-command "myfile-opener"
             :css-dirs ["resources/public/css"]}
  :profiles
  {:dev  [:project/dev  :profiles/dev]
   :test [:project/test :profiles/test]
   :uberjar {:aot :all}
   :profiles/dev  {}
   :profiles/test {}
   :project/dev   {:source-paths ["dev"]
                   :repl-options {:init-ns user}
                   :dependencies [[reloaded.repl "0.1.0"]
                                  [org.clojure/tools.namespace "0.2.11"]
                                  [kerodon "0.6.1"]
                                  [org.clojure/tools.nrepl "0.2.10"]]
                   :env {:port 3000}}
   :project/test  {}})
