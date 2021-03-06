(defproject shopping-list "0.1.0-SNAPSHOT"
  :description "Shopping list"
  :url "http://kauppalista.tiuhti.net"
  :min-lein-version "2.5.0"
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                   :username :env
                                   :password :env}}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.stuartsierra/component "0.3.0"]
                 [compojure "1.4.0"]
                 [duct "0.4.5"]
                 [meta-merge "0.1.1"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring-jetty-component "0.3.0"]
                 [ring-transit "0.1.4"]
                 [prismatic/plumbing "0.5.2"]
                 [com.datomic/datomic-pro "0.9.5201" :exclusions [[joda-time]
                                                                  [org.apache.httpcomponents/httpclient]]]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"]
                 [buddy/buddy-hashers "0.6.0"]
                 [org.clojure/tools.nrepl "0.2.11"]

                 [org.clojure/clojurescript "1.7.170"]
                 [cljs-ajax "0.5.1"]
                 [reagent "0.5.1" :exclusions [cljsjs/react]]
                 [cljsjs/react-with-addons "0.14.3-0"]]
  :plugins [[lein-gen "0.2.2"]
            [lein-figwheel "0.5.0-1"]
            [lein-cljsbuild "1.1.1"]
            [com.jakemccrary/lein-test-refresh "0.10.0"]
            [lein-doo "0.1.6-SNAPSHOT"]]
  :generators [[duct/generators "0.1.2"]]
  :duct {:ns-prefix shopping-list}
  :main ^:skip-aot shopping-list.main
  :uberjar-name "shopping-list.jar"
  :aliases {"gen"   ["generate"]
            "setup" ["do" ["generate" "locals"]]}
  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj" "test/cljc"]
  :cljsbuild {:builds {:dev {:source-paths ["src/cljs" "src/cljs-app-config"]
                             :figwheel {:on-jsload "shopping-list.app/main"
                                        :websocket-host "172.16.0.62"}
                             :compiler {:main shopping-list.app
                                        :output-to "resources/public/js/app.js"
                                        :output-dir "resources/public/js/out"
                                        :asset-path "js/out"
                                        :optimizations :none
                                        :pretty-print true
                                        :source-map-timestamp true
                                        :cache-analysis true}}
                       :test {:source-paths ["src/cljs" "test/cljs" "test/cljs-app-config"]
                              :compiler {:main shopping-list.unit-runner
                                         :output-to "resources/public/js/test.js"
                                         :optimizations :none}}
                       :prod {:source-paths ["src/cljs" "src/cljs-app-config"]
                              :compiler {:output-to "resources/public/js/app.js"
                                         :optimizations :advanced}}}}
  :figwheel {:nrepl-port 7889
             :open-file-command "~/bin/figwheel-open"
             :css-dirs ["resources/public/css"]
             :nrepl-middleware ["cider.nrepl/cider-middleware"
                                "refactor-nrepl.middleware/wrap-refactor"
                                "cemerick.piggieback/wrap-cljs-repl"]}
  :clean-targets ^{:protect false} ["resources/public/js/app.js"
                                    "resources/public/js/out"
                                    :target-path]
  :target-path "target/%s/"
  :profiles
  {:uberjar {:aot :all
             :prep-tasks ^:replace ["clean"
                                    ["cljsbuild" "once" "prod"]
                                    "javac"
                                    "compile"]}
   :dev {:source-paths ["dev"]
         :repl-options {:init-ns user}
         :dependencies [[reloaded.repl "0.2.1"]
                        [org.clojure/tools.namespace "0.2.11"]
                        [org.clojure/tools.nrepl "0.2.10"]
                        [peridot "0.4.1"]
                        [figwheel-sidecar "0.5.0-1"]
                        [com.cemerick/piggieback "0.2.1"]
                        [lein-doo "0.1.6-SNAPSHOT"]
                        [cljs-react-test "0.1.3-SNAPSHOT"]
                        [prismatic/dommy "1.1.0"]
                        [ring-cors "0.1.7"]
                        [eftest "0.1.0"]]}})
