(ns ga4gh.reference-api.tools
  (:require
   [cljs.nodejs :as nodejs]
   [cljs.test :refer-macros [is]]
   [dmohs.requests :as r]
   [ga4gh.reference-api.testing :refer-macros [defswaggertest]]
   [ga4gh.reference-api.utils :as u]
   ))

(def fs (nodejs/require "fs"))
(def yaml (nodejs/require "js-yaml"))


(def tool-data
  (let [yaml-string (.readFileSync fs "/etc/tool-data.yaml" "utf-8")
        parsed (.load yaml yaml-string)
        cljs (js->clj parsed :keywordize-keys true)]
    (get cljs :tools)))


(defn get-tool [ctx]
  (let [[id-string] (-> ctx :request :url-params)
        id-string (js/decodeURIComponent id-string)
        tool (first (filter #(= (:registry-id %) id-string) tool-data))]
    (if tool
      (-> ctx
          (r/json-body tool)
          r/respond)
      (-> ctx
          (r/status-code 404)
          (r/json-body {:error :not-found :message "Tool not found"})
          (r/respond)))))


(defswaggertest get-tools-id
  :get-tools-id
  {:id (get-in tool-data [0 :registry-id])}
  (fn [obj]
    (is (= (obj "registry-id") (get-in tool-data [0 :registry-id])))))


(defn get-tool-version [ctx]
  (let [params (-> ctx :request :url-params)
        [id-string version-id-string] (map js/decodeURIComponent params)
        tool (first (filter #(= (:registry-id %) id-string) tool-data))
        version (first (filter #(= (:registry-id %) version-id-string) (:versions tool)))]
    (if version
      (-> ctx
          (r/json-body version)
          r/respond)
      (-> ctx
          (r/status-code 404)
          (r/json-body {:error :not-found :message "Tool version not found"})
          (r/respond)))))


(defswaggertest get-tools-id-versions-version-id
  :get-tools-id-versions-version-id
  {:id (get-in tool-data [0 :registry-id]) :version-id (get-in tool-data [0 :versions 0 :name])}
  (fn [obj]
    (is (= (obj "name") (get-in tool-data [0 :versions 0 :name])))))
