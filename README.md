# Kafka claim check pattern example

Sending large messages through Kafka is not recommended. It is often beneficial to offload the message payload and transport the refence through Kafka.

This is known as the [claim check pattern](https://www.enterpriseintegrationpatterns.com/patterns/messaging/StoreInLibrary.html)

This example contains a simple illustration of the concept using [Min.io](https://min.io/), an S3-compatible object store.

## run tests

`> sbt dockerComposeTest`

## caveat

The implementation demonstrates a naive approach. In order to be used in production, more aspects need to be covered, e.g.:

* error handling
* authentication
* retention
* metadata

Sometimes, if the test fails, the docker containers will not be stopped. You will need to stop them manually. 

`> docker ps -q | xargs docker stop ` 

## have fun and help improve the project with your pull requests
