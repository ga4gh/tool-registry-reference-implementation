FROM node:5

RUN true \
  && mkdir /app \
  && cd /app \
  && curl -LO \
    https://github.com/dmohs/ga4gh-tool-registry-reference/releases/download/0.0.2/target.tar.gz \
  && gunzip target.tar.gz \
  && tar -xf target.tar \
  && rm target.tar

WORKDIR /app
ENTRYPOINT node target/main.js
