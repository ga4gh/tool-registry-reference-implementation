(defproject ga4gh/reference-api "0.0.1"
  :dependencies [
                 [dmohs/requests "0.0.1"]
                 [org.clojure/clojure "1.7.0"] ; keep up-to-date with clojurescript dep
                 [org.clojure/clojurescript "1.7.228"]
                 ]
  :plugins [[lein-cljsbuild "1.1.2"] [lein-figwheel "0.5.0-6"]]
  :profiles {:dev {:cljsbuild
                   {:builds
                    {:client
                     {:figwheel {:websocket-url ~(str "ws://server-figwheel:3449/figwheel-ws")}
                      :compiler {:output-dir "target"
                                 :output-to "target/main.js"
                                 :source-map true}}}}}
             :release {:cljsbuild
                       {:builds
                        {:client
                         {:compiler {:output-dir "target"
                                     :output-to "target/main.js"}}}}}}
  :cljsbuild {:builds {:client {:source-paths ["src/cljs"]
                                :compiler
                                {:target :nodejs
                                 :main ga4gh.reference-api.main}}}})
