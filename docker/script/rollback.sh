#!/bin/bash
set -e

DOCKER_USER_NAME=namgungjaeseon
IMAGE_NAME=thred_app
CONTAINER_NAME=thred_app

echo "[ROLLBACK] Starting rollback process..."
docker compose -f thred/docker/thred-compose.yaml down

# 최신에서 두 번째 태그(이전 버전) 가져오기
PREVIOUS_TAG_ID=$(docker images --format "{{.Repository}}:{{.Tag}}" \
  | grep -i "${IMAGE_NAME}:" \
  | awk -F: '{print $2}' \
  | sort -r \
  | head -n 2 \
  | tail -n 1)

if [ -z "$PREVIOUS_TAG_ID" ]; then
  echo "[ERROR] No previous image found for rollback."
  exit 1
fi

## 컨테이너 삭제 (존재할 때만)
if [ "$(docker ps -a -q -f name=^/${CONTAINER_NAME}$)" ]; then
  echo "[CLEANUP] Removing existing container '${CONTAINER_NAME}'..."
  docker rm -f "${CONTAINER_NAME}"
else
  echo "[CLEANUP] No existing container found for '${CONTAINER_NAME}'."
fi

# 최신 이미지는 삭제하지 않고, 이전 버전을 재실행
echo "TAG_ID=${PREVIOUS_TAG_ID}" > .env
set -a
source .env
set +a

echo "[ROLLBACK] Restarting container with previous image tag: ${PREVIOUS_TAG_ID}"
docker compose -f thred/docker/thred-compose.yaml up -d

echo "[ROLLBACK] Completed successfully. Rolled back to Tag: $TAG_ID"
