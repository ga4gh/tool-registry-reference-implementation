(ns ga4gh.reference-api.test-data
  (:require
   [cljs.nodejs :as nodejs]
   [clojure.string :as cs]
   [ga4gh.reference-api.utils :as u]
   ))

(def yaml (nodejs/require "js-yaml"))


(def org-name "Acme Seqencing Corporation")
(def tool-types
  [{:id "75015d4c-5001-41b6-937c-de58e0b7e42f"
    :name "wdl-workflow"
    :description "A workflow with a WDL descriptor."}])
(def tool-data
  {:tools
   [(let [namespace "acme"
          n "loud-echo"
          url (str "http://tool-registry.example.com/tools/" namespace "/" n)]
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
}"}}]})]})


(defn dump-to-yaml-string []
  (.dump yaml (clj->js tool-data) (clj->js {:sortKeys true})))
