(ns ga4gh.reference-api.main-test
  (:require
   [cljs.nodejs :as nodejs]
   [cljs.test :refer-macros [deftest async is run-tests]]
   [ga4gh.reference-api.utils :as u]
   ))

(def swagger (nodejs/require "swagger-client"))

(nodejs/enable-util-print!)


(def branch "develop")
(def yaml-url (str "https://raw.githubusercontent.com/ga4gh/tool-registry-schemas"
                   "/" branch
                   "/src/main/resources/swagger/ga4gh-tool-discovery.yaml"))
(def client nil)


(deftest root
  (is (not (nil? client)))
  #_(.help (.-get_tools_id (.-GA4GH client)))
  (async done
         (.get_tools_id (.-GA4GH client) (clj->js {:id "dmohs-test/docker-test"})
                        (fn [res]
                          (is (= (aget res "obj" "registry-id") "dmohs-test/docker-test"))
                          (done))
                        (fn [res]
                          (is (nil? (.-errObj res)))
                          (done)))))


(defn -main [& args]
  (set! client (swagger.
                (clj->js
                 {:url yaml-url
                  :success (fn []
                             (.setSchemes client (clj->js ["http"]))
                             (.setHost client "server-dev")
                             (run-tests))}))))


(set! *main-cli-fn* -main)


(-main)
