FROM node:5

RUN true \
  && mkdir /project \
  && cd /project \
  && curl -LO \
    https://github.com/dmohs/ga4gh-tool-registry-reference/releases/download/0.0.1/target.tar.gz \
  && gunzip target.tar.gz \
  && tar -xf target.tar \
  && rm target.tar

WORKDIR /project
ENTRYPOINT node target/main.js
