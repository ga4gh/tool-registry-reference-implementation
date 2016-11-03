# Tool Registry API Reference Implementation

## Running the server

```bash
docker run --name ga4gh-ref-api-server -d -p 80:80 quay.io/ga4gh/ga4gh-tool-registry-reference
```

## Running tests

```bash
docker exec ga4gh-ref-api-server node target/main.js run-tests
```

## Overriding the included set of sample tool data

```bash
docker create --name ga4gh-ref-api-server -d -p 80:80 quay.io/ga4gh/ga4gh-tool-registry-reference
docker cp my-tool-data.yaml ga4gh-ref-api-server:/etc/tool-data.yaml
docker start ga4gh-ref-api-server
```

## Building the Docker images

Create the target.tar.gz that needs to be pointed at from the Dockerfile

```bash
bash src/container/release/build-release.sh
```
