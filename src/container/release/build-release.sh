#!/bin/bash
IFS=$'\n\t'
set -euxo pipefail


CNAME="$(
  docker create -w /w -v jars:/root/.m2 \
  clojure \
  lein cljsbuild once
)"
COPYFILE_DISABLE=1 tar -c project.clj src/cljs | docker cp - "$CNAME":/w
docker start --attach "$CNAME"
docker cp "$CNAME":/w/target .
tar -cf target.tar target
rm -rf target
gzip target.tar
