#!/bin/bash
set -e  # 하나라도 실패하면 스크립트 중단

DOCKER_USER_NAME=namgungjaeseon
IMAGE_NAME=thred_app
CONTAINER_NAME=thred_app
TAG_ID=$1

echo "[INFO] Deploying image: ${DOCKER_USER_NAME}/${IMAGE_NAME}:${TAG_ID}"

# 1. 기존 컨테이너 종료
echo "[INFO] Shutting down existing containers..."
docker compose -f thred/docker/thred-compose.yaml down app

# 2. 최근 2개 태그만 남기고 나머지 삭제 (현재 배포할 TAG_ID 포함)
if [ "$(docker images -q ${DOCKER_USER_NAME}/${IMAGE_NAME})" ]; then
  echo "[CLEANUP] Keeping only the latest 2 images (rollback safe)..."

  IMAGE_TAGS=$(docker images --format "{{.Tag}}" ${DOCKER_USER_NAME}/${IMAGE_NAME} \
    | sort -r)
  # 최신 2개 태그 확보 (TAG_ID 포함 + 이전 최신)
  LATEST_TWO=$(echo "$IMAGE_TAGS" | head -n 2)

  for tag in $IMAGE_TAGS; do
    if [[ ! " $LATEST_TWO " =~ " $tag " ]]; then
      echo "[CLEANUP] Removing old image: ${DOCKER_USER_NAME}/${IMAGE_NAME}:${tag}"
      docker rmi "${DOCKER_USER_NAME}/${IMAGE_NAME}:${tag}" || true
    fi
  done
else
  echo "[CLEANUP] No images found for ${DOCKER_USER_NAME}/${IMAGE_NAME}."
fi

# 3. 환경 변수 파일 업데이트
echo "TAG_ID=${TAG_ID}" > .env
set -a
source .env
set +a

# 4. 신규 컨테이너 실행
echo "[DEPLOY] Starting new container with image ${DOCKER_USER_NAME}/${IMAGE_NAME}:${TAG_ID}..."
docker pull ${DOCKER_USER_NAME}/${IMAGE_NAME}:${TAG_ID}
docker compose -f thred/docker/thred-compose.yaml up -d app
