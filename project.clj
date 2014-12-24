(defproject pult "0.0.1"
  :description "Remote control for Game emulators"
  :url "https://github.com/tauho/pult"
  :license {:name "MIT"
            :comments "MIT license"}
  :min-lein-version "2.5.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [org.clojure/core.async  "0.1.346.0-17112a-alpha"]
                 [reagent "0.4.3"]
                 [reagent/reagent-cursor "0.1.2"]
                 [secretary "1.2.1"]
                 [environ "1.0.0"]
                 [com.cemerick/piggieback "0.1.3"]
                 [weasel "0.4.0-SNAPSHOT"]
                 [jarohen/chord "0.4.2" :exclusions [org.clojure/clojure]]
                 [cljs-idxdb "0.1.0"]
                 [garden "1.2.5"]]
  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-simpleton "1.3.0"]
            [lein-garden "0.2.5"]]
  :source-paths ["src"]
  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :preamble ["js/vendor/react.js"]
  :cljsbuild {:repl-launch-commands {
                ;usage: lein trampoline cljsbuild repl-launch firefox
                "firefox" ["firefox"] ;; to browse up on browser
                "firefox-naked" ["firefox"
                                 "naked.html"
                                 :stdout ".repl-firefox-out"
                                 :stderr ".repl-firefox-err"] ;; to interact via REPL
              }
              :test-commands {"unit" ["phantomjs" "test/unit-tests.js" "test/index.html"]}
              :builds {:dev {:source-paths ["src"]
                             :compiler {:output-to "js/pult-dev.js"
                                        :optimizations :whitespace
                                        :pretty-print true}}
                       :prod {:source-paths ["src"]
                              :compiler {:output-to "js/pult.js"}
                              :optimizations :advanced
                              :pretty-print false}
                       :test {:source-paths ["src" "test"]
                              :compiler {:output-to "test/unit-tests.js"
                                         :optimizations :whitespace
                                         :pretty-print true}}}}
  :garden {:builds [{:id "app"
                     :source-paths ["garden"]
                     :stylesheet pult.app/styles
                     :compiler {:output-to "css/app.css"
                                :pretty-print? true}}]})
