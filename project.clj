(defproject ga4gh/reference-api "0.0.1"
  :dependencies [
                 [dmohs/requests "0.0.1"]
                 [org.clojure/clojure "1.8.0"] ; keep up-to-date with clojurescript dep
                 [org.clojure/clojurescript "1.9.36"]
                 ]
  :plugins [[lein-cljsbuild "1.1.3"] [lein-figwheel "0.5.0-6" :exclusions [org.clojure/clojure]]]
  :profiles {:dev {:cljsbuild
                   {:builds
                    {:client
                     {:figwheel {:websocket-url "ws://ga4ghref-server-builder:3449/figwheel-ws"}
                      :compiler {:source-map true}}}}}}
  :cljsbuild {:builds {:client {:source-paths ["src/cljs/core"]
                                :compiler
                                {:target :nodejs
                                 :output-dir "target"
                                 :output-to "target/main.js"
                                 :main ga4gh.reference-api.main
                                 :optimizations :none}}}})
