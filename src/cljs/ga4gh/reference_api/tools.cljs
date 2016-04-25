(ns ga4gh.reference-api.tools
  (:require
   [cljs.nodejs :as nodejs]
   [dmohs.requests :as r]
   [ga4gh.reference-api.utils :as u]
   ))


(def org-name "Broad Institute")
(def tool-types
  [{:id "75015d4c-5001-41b6-937c-de58e0b7e42f"
    :name "wdl-workflow"
    :description "A workflow with a WDL descriptor."}])
(def tool-data
  [(let [namespace "dmohs_test"
         n "docker-test"
         url (str "http://ga4gh-api.broadinstitute.org/tools/" namespace "/" n)]
     {:global-id url ; -> url
      :registry-id (str namespace "/" n) ; -> id
      :registry "docker.io"
      :organization org-name
      :name "ubuntu" ; -> image-name
      :toolname "Ubuntu"
      :tooltype (get tool-types 0)
      :description "A simple workflow that produces some output."
      :author "dmohs@broadinstitute.org"
      :meta-version "1"
      :versions [{:name "1"
                  :global-id (str url "/1") ; -> url
                  :registry-id "1" ; -> version-id
                  :registry "docker.io"
                  :image "ubuntu:latest"
                  :descriptor {:descriptor "task echo_files {
  File html_file
  File image_file

  output {
    File out_html_file = \"out.html\"
    File out_image_file = \"image.gif\"
  }

  command {
    cp ${html_file} \"out.html\"
    cp ${image_file} \"image.gif\"
  }

  runtime {
    docker: \"ubuntu:latest\"
  }
}

workflow HTML_Report {
  call echo_files
}"}}]})])


(defn get-tool [ctx]
  (let [[id-string] (-> ctx :request :url-params)
        tool (first (filter #(= (:registry-id %) id-string) tool-data))]
    (if tool
      (-> ctx
          (r/json-body tool)
          r/respond)
      (-> ctx
          (r/status-code 404)
          (r/json-body {:error :not-found :message "Tool not found"})
          (r/respond)))))


(defn get-tool-version [ctx]
  (let [[id-string version-id-string] (-> ctx :request :url-params)
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
