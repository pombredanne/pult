(ns pult.repl
  (:require [weasel.repl :as repl]
            ;[clojure.browser.repl :as repl]
            ;[clojure.reflect :as reflect]
            ))


;(repl/connect "http://localhost:9000/repl") ;for browser.repl
;(if-not (repl/alive?)
;  (repl/connect "ws://localhost:9001"))

(comment
  ;; getting started with weasel
  ;; >lein repl
  (require 'weasel.repl.websocket)
  (cemerick.piggieback/cljs-repl :repl-env (weasel.repl.websocket/repl-env))

  ;;or
  (cemerick.piggieback/cljs-repl
    :repl-env (weasel.repl.websocket/repl-env
                :ip "0.0.0.0" :port 9001))

  ;; open repl.html and connections should be opened
  (do (js/alert "Hello world!") 42)
  (= (js/Number. 34) (js/Number. 34))

  )
