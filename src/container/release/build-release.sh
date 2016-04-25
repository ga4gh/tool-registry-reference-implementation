#!/bin/bash
IFS=$'\n\t'
set -euxo pipefail


docker exec server-figwheel rm -rf /tmp/project-deploy
docker exec server-figwheel mkdir /tmp/project-deploy
COPYFILE_DISABLE=1 tar -c project.clj src/cljs | docker cp - server-figwheel:/tmp/project-deploy
docker exec server-figwheel bash -c \
  'cd /tmp/project-deploy; lein with-profile release cljsbuild once'
docker cp server-figwheel:/tmp/project-deploy/target .
