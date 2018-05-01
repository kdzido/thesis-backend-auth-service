#!/usr/bin/env sh

echo "-> Waiting for any Eureka peer to start on port $EUREKASERVICE_PORT...."
while (! `nc -z eurekapeer1 $EUREKASERVICE_PORT`) && (! `nc -z eurekapeer2 $EUREKASERVICE_PORT`); do sleep 3; done
echo "--> Eureka service has started"

echo "-> Waiting for the Cloud Config service to start on port $CONFIGSERVICE_PORT..."
while ! `nc -z configservice $CONFIGSERVICE_PORT`; do sleep 3; done
echo "--> Cloud Config service has started"

# TODO wait for zipkin

echo "-> Starting OAuth2 service"
java    -Djava.security.egd=file:/dev/./urandom \
        -Dserver.port=$SERVER_PORT \
        -Deureka.client.serviceUrl.defaultZone=$EUREKASERVICE_URI \
        -Dspring.cloud.config.uri=$CONFIGSERVICE_URI \
        -Dspring.profiles.active=$PROFILE \
        -jar /usr/local/auth-service/@springBootJar@
