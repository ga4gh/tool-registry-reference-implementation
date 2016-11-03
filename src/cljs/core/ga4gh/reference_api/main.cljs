(ns ga4gh.reference-api.main
  (:require
   [cljs.nodejs :as nodejs]
   [cljs.test :refer-macros [is]]
   [dmohs.requests :as r]
   [ga4gh.reference-api.tools :as tools]
   [ga4gh.reference-api.test-data :as test-data]
   [ga4gh.reference-api.testing :refer-macros [defswaggertest] :as testing]
   [ga4gh.reference-api.utils :as u]
   ))

(def http (nodejs/require "http"))

(nodejs/enable-util-print!)


(defn- index [ctx]
  (-> ctx
      (r/json-body
       [{:url "/tools/{id-string}" :description "Returns a tool description"}])
      r/respond))


(defn- ping [ctx]
  (r/respond ctx))


(defn- commit-suicide [ctx]
  ((or false nil) 17))


(defn- get-metadata [ctx]
  (-> ctx
      (r/json-body
       {:version "1"
        :api-version "1"
        :country "US"
        :friendly-name "GA4GH Tools API Reference"})))


(defn- handle-request [req res]
  (let [ctx (r/create-context req res)
        url (:url (:request ctx))
        disable-logging? (contains? #{"/ping"} url)
        ctx (merge ctx (when disable-logging? {:disable-logging? true}))]
    (when-not disable-logging?
      (.log js/console
            (str "Received request: " (:id ctx) " " (:client-ip (:request ctx)) " " url)))
    (some->
     ctx
     (r/handle-url #"/" #{:get :post} index)
     (r/handle-url #"/ping" #{:get} ping)
     (r/handle-url #"/commit-suicide" #{:post} commit-suicide)
     (r/handle-url #"/api/ga4gh/v1/tools/([%A-Za-z0-9_-]+)" #{:get} tools/get-tool)
     (r/handle-url #"/api/ga4gh/v1/tools/([%A-Za-z0-9_-]+)/versions" #{:get} tools/get-tool-versions)
     (r/handle-url #"/api/ga4gh/v1/tools/([%A-Za-z0-9_-]+)/versions/([%A-Za-z0-9_-]+)"
                   #{:get} tools/get-tool-version)
     (r/handle-url #"/api/ga4gh/v1/tools" #{:get} tools/get-tools)
     (r/handle-url #"/api/ga4gh/v1/tools/([%A-Za-z0-9_-]+)/versions/([%A-Za-z0-9_-]+)/([a-z]+)/descriptor"
                   #{:get} tools/get-tool-descriptor)
     (r/handle-url
      #"/api/ga4gh/v1/tools/([%A-Za-z0-9_-]+)/versions/([%A-Za-z0-9_-]+)/([a-z]+)/descriptor/([%A-Za-z0-9_-]+)"
      #{:get} tools/get-tool-descriptor-child)
     (r/handle-url #"/api/ga4gh/v1/tools/([%A-Za-z0-9_-]+)/versions/([%A-Za-z0-9_-]+)/dockerfile"
                   #{:get} tools/get-tool-version-dockerfile)
     (r/handle-url #"/api/ga4gh/v1/metadata" #{:get} get-metadata)
     (r/handle-url #"/api/ga4gh/v1/tool-types" #{:get} tools/get-tool-types)
     r/respond-with-not-found)))


;; Allow the request handler to be hot reloaded (maybe http.createServer hangs on to it?)
(defonce request-handler (atom nil))
(reset! request-handler handle-request)


(defn -main [& [command]]
  (if command
    (cond
      (= command "dump-sample-config")
      (println (test-data/dump-to-yaml-string))
      (= command "run-tests")
      (do
        (tools/load-tool-data)
        (testing/initialize
         #(cljs.test/run-tests 'ga4gh.reference-api.main 'ga4gh.reference-api.tools)))
      :else
      (do
        (println (str "Run without arguments to start the server. Run with dump-sample-config"
                      " to dump the sample YAML configuration to stdout."))
        (.exit js/process 1)))
    (do
      (tools/load-tool-data)
      (-> (.createServer http (fn [req res] (@request-handler req res)))
          (.listen 80))
      (println "Server running on port 80."))))

(set! *main-cli-fn* -main)


(defswaggertest test-metadata
  :get-metadata
  {}
  (fn [obj]
    (is (= (obj "country") "US"))))
