#!/bin/bash
set -e

DOCKER_USER_NAME=namgungjaeseon
IMAGE_NAME=thred_app
TAG_ID=$1

echo "=> Build new image..."
docker build --tag ${DOCKER_USER_NAME}/${IMAGE_NAME}:${TAG_ID} -f Dockerfile . --platform=linux/amd64

echo "=> Push container..."
docker push ${DOCKER_USER_NAME}/${IMAGE_NAME}:${TAG_ID}

docker rmi ${DOCKER_USER_NAME}/${IMAGE_NAME}:${TAG_ID} || true

echo "Docker ID: ${TAG_ID}"
