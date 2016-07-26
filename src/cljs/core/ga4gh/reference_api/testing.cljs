(ns ga4gh.reference-api.testing
  (:require-macros
   [ga4gh.reference-api.testing :refer [defswaggertest]])
  (:require
   [cljs.nodejs :as nodejs]
   [cljs.test :refer-macros [deftest async is]]
   [clojure.string :as cs]
   [dmohs.requests :as r]
   [ga4gh.reference-api.utils :as u]
   ))

(def swagger (nodejs/require "swagger-client"))


(def branch "develop")
(def yaml-url (str "https://raw.githubusercontent.com/ga4gh/tool-registry-schemas"
                   "/" branch
                   "/src/main/resources/swagger/ga4gh-tool-discovery.yaml"))
(defonce methods-root nil)


(defn initialize
  ([] (initialize #(println "Swagger client initialized.")))
  ([on-done]
   (let [client (atom nil)]
     (reset! client (swagger.
                     (clj->js
                      {:url yaml-url
                       :success (fn []
                                  (.setSchemes @client (clj->js ["http"]))
                                  (.setHost @client "ga4ghref-server-dev")
                                  (set! methods-root (.-GA4GH @client))
                                  ;; (.help methods-root)
                                  (when on-done
                                    (on-done @client)))}))))))


(defn test-request [method-keyword params on-success]
  (assert methods-root)
  (async
   done
   ;; Run on nextTick to avoid any output in REPL so user is directed to the process which has all
   ;; output.
   (.nextTick
    js/process
    (fn []
      (let [method-name (cs/replace (name method-keyword) "-" "_")
            method (aget methods-root method-name)]
        (assert (not (nil? method)))
        (method (clj->js params)
                (fn [res]
                  (let [obj (aget res "obj")]
                    (if (not (nil? obj))
                      (on-success (js->clj obj))
                      (cljs.test/do-report {:type :fail
                                            :message "Unexpected success without result object"}))
                    (done)))
                (fn [err]
                  (cljs.test/do-report {:type :fail
                                        :message "Unexpected error on Swagger API call"
                                        :expected nil
                                        :actual err})
                  (done))))))))


;; Run at REPL:
;; (require 'ga4gh.reference-api.testing)
;; (ga4gh.reference-api.testing/run-tests)
