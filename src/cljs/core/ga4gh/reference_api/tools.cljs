(ns ga4gh.reference-api.tools
  (:require
   [cljs.nodejs :as nodejs]
   [cljs.test :refer-macros [is]]
   [dmohs.requests :as r]
   [ga4gh.reference-api.test-data :as test-data]
   [ga4gh.reference-api.testing :refer-macros [defswaggertest]]
   [ga4gh.reference-api.utils :as u]
   ))

(def fs (nodejs/require "fs"))
(def yaml (nodejs/require "js-yaml"))


(def tool-types nil)
(def tool-data nil)


(defn load-tool-data []
  (let [yaml-string (.readFileSync fs "/etc/tool-data.yaml" "utf-8")
        ;; yaml-string (test-data/dump-to-yaml-string)
        parsed (.load yaml yaml-string)
        cljs (js->clj parsed :keywordize-keys true)]
    (set! tool-types (get cljs :tool-types))
    (set! tool-data (get cljs :tools))))


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


(defn get-tool-versions [ctx]
  (let [[id-string] (-> ctx :request :url-params)
        id-string (js/decodeURIComponent id-string)
        tool (first (filter #(= (:registry-id %) id-string) tool-data))]
    (if tool
      (-> ctx
          (r/json-body (:versions tool))
          r/respond)
      (-> ctx
          (r/status-code 404)
          (r/json-body {:error :not-found :message "Tool not found"})
          (r/respond)))))


(defswaggertest get-tools-versions
  :get-tools-id-versions
  {:id (get-in tool-data [0 :registry-id])}
  (fn [obj]
    (is (= (get-in obj [0 "name"]) (get-in tool-data [0 :versions 0 :name])))))


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


(defn get-tools [ctx]
  (-> ctx
      (r/json-body tool-data)
      r/respond))


(defswaggertest test-get-tools
  :get-tools
  {}
  (fn [obj]
    (is (= (count obj) (count tool-data)))))


(defn get-tool-descriptor [ctx]
  (let [params (-> ctx :request :url-params)
        [id-string version-id-string type-name] (map js/decodeURIComponent params)
        tool (first (filter #(= (:registry-id %) id-string) tool-data))
        version (first (filter #(= (:registry-id %) version-id-string) (:versions tool)))
        descriptor (first (filter #(= (:type %) type-name) (:descriptors version)))]
    (cond
      (nil? tool)
      (-> ctx
          (r/status-code 404)
          (r/json-body {:error :not-found :message "Tool not found"})
          r/respond)
      (nil? version)
      (-> ctx
          (r/status-code 404)
          (r/json-body {:error :not-found :message "Tool version not found"})
          r/respond)
      (nil? descriptor)
      (-> ctx
          (r/status-code 404)
          (r/json-body {:error :not-found
                        :message "Descriptor not found for specified tool version"})
          r/respond)
      :else
      (-> ctx
          (r/json-body descriptor)
          r/respond))))


(defswaggertest test-get-tool-descriptor
  :get-tools-id-versions-version-id-type-descriptor
  {:id (get-in tool-data [0 :registry-id]) :version-id (get-in tool-data [0 :versions 0 :name])
   :type (get-in tool-data [0 :versions 0 :descriptors 0 :type])}
  (fn [obj]
    (is (= (obj "type") (get-in tool-data [0 :versions 0 :descriptors 0 :type])))))


(defn get-tool-descriptor-child [ctx]
  (let [params (-> ctx :request :url-params)
        [id-string version-id-string type-name relative-path] (map js/decodeURIComponent params)
        tool (first (filter #(= (:registry-id %) id-string) tool-data))
        version (first (filter #(= (:registry-id %) version-id-string) (:versions tool)))
        descriptor (first (filter #(= (:type %) type-name) (:descriptors version)))
        child (first (filter #(= (:relative-path %) relative-path) (:children descriptor)))]
    (cond
      (nil? tool)
      (-> ctx
          (r/status-code 404)
          (r/json-body {:error :not-found :message "Tool not found"})
          r/respond)
      (nil? version)
      (-> ctx
          (r/status-code 404)
          (r/json-body {:error :not-found :message "Tool version not found"})
          r/respond)
      (nil? descriptor)
      (-> ctx
          (r/status-code 404)
          (r/json-body {:error :not-found
                        :message "Descriptor not found for specified tool version"})
          r/respond)
      :else
      (-> ctx
          (r/json-body descriptor)
          r/respond))))


(defswaggertest test-get-tool-descriptor-child
  :get-tools-id-versions-version-id-type-descriptor-relative-path
  {:id (get-in tool-data [0 :registry-id]) :version-id (get-in tool-data [0 :versions 0 :name])
   :type (get-in tool-data [0 :versions 0 :descriptors 0 :type])
   :relative-path (get-in tool-data [0 :versions 0 :descriptors 0 :children 0 :relative-path])}
  (fn [obj]
    (is (= (obj "type") (get-in tool-data [0 :versions 0 :descriptors 0 :children 0 :type])))))


(defn get-tool-version-dockerfile [ctx]
  (let [params (-> ctx :request :url-params)
        [id-string version-id-string] (map js/decodeURIComponent params)
        tool (first (filter #(= (:registry-id %) id-string) tool-data))
        version (first (filter #(= (:registry-id %) version-id-string) (:versions tool)))]
    (cond
      (nil? tool)
      (-> ctx
          (r/status-code 404)
          (r/json-body {:error :not-found :message "Tool not found"})
          r/respond)
      (nil? version)
      (-> ctx
          (r/status-code 404)
          (r/json-body {:error :not-found :message "Tool version not found"})
          r/respond)
      :else
      (-> ctx
          (r/json-body (:dockerfile version))
          r/respond))))


(defswaggertest test-get-tool-version-dockerfile
  :get-tools-id-versions-version-id-dockerfile
  {:id (get-in tool-data [0 :registry-id])
   :version-id (get-in tool-data [0 :versions 0 :registry-id])}
  (fn [obj]
    (is (= (obj "dockerfile") (get-in tool-data [0 :versions 0 :dockerfile :dockerfile])))))


(defn get-tool-types [ctx]
  (-> ctx
      (r/json-body tool-types)
      r/respond))


(defswaggertest test-get-tool-types
  :get-tool-types
  {}
  (fn [obj]
    (is (= (get-in obj [0 "id"]) (get-in tool-types [0 :id])))))
