#!/usr/bin/env bash
echo "Building started for Signifyd connector functions"
docker build -f Functions.Dockerfile -t signifyd-functions .
docker container create --name extract signifyd-functions
docker container cp extract:./home/app/signifyd-connector/wrappers/cloud/aws-lambda/target/aws-lambda-1.0-SNAPSHOT.jar ./builds/aws-lambda.jar
docker container cp extract:./home/app/signifyd-connector/wrappers/cloud/gcp/target/gcp-1.0-SNAPSHOT.jar ./builds/gcp.jar
docker container rm -f extract
echo "Building has been completed"
