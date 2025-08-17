# 1) 런타임 JRE 만들기
FROM amazoncorretto:17-alpine3.18 AS builder-jre
RUN apk add --no-cache --repository=http://dl-cdn.alpinelinux.org/alpine/edge/main/ binutils
RUN $JAVA_HOME/bin/jlink \
    --module-path "$JAVA_HOME/jmods" \
    --add-modules ALL-MODULE-PATH \
    --strip-debug --no-man-pages --no-header-files \
    --compress=2 --output /jre

# 2) 빌드
FROM gradle:8.5-jdk17 AS build
WORKDIR /thred-server
# 의존성 먼저 복사
COPY settings.gradle build.gradle ./
COPY gradle gradle
COPY gradlew ./
RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon || true

# 소스 복사
COPY src src

# 빌드 (테스트 스킵)
RUN ./gradlew clean bootJar --no-daemon --build-cache -x test

# 3) 런타임
FROM alpine:3.18.4
ENV JAVA_HOME=/jre
ENV PATH="$JAVA_HOME/bin:$PATH"
RUN apk add --no-cache ca-certificates tzdata
COPY --from=builder-jre /jre $JAVA_HOME
WORKDIR /app
COPY --from=build /thred-server/build/libs/*.jar thred-server.jar
ENTRYPOINT ["java", "-jar", "thred-server.jar"]
