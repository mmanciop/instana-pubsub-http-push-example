# Instana Google Cloud Pub/Sub Push notification demo

This repository provides an example of using the Instana Java SDK to retain trace context over Google Cloud Pub/Sub HTTP-based push notifications.

## Configure

Create a `.env` file in the root of the checked-out version of this repository and enter the following text, with the values adjusted as necessary:

```text
agent_key=<TODO FILL UP>
agent_endpoint=<local ip or remote host; e.g., saas-us-west-2.instana.io>
agent_endpoint_port=<443 already set as default>
agent_zone=<name of the zone for the agent; default: pubsub-push-demo>
```

## Build

```sh
./mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=pubsub-notification-consumer
```

## Run

```sh
docker-compose up
```
