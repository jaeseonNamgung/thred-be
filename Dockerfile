FROM amazoncorretto:17-alpine3.18 AS builder-jre
RUN apk add --no-cache --repository=http://dl-cdn.alpinelinux.org/alpine/edge/main/ binutils
RUN $JAVA_HOME/bin/jlink \
    --module-path "$JAVA_HOME/jmods" \
        --verbose \
        --add-modules ALL-MODULE-PATH \
        --strip-debug \
        --no-man-pages \
        --no-header-files \
        --compress=2 \
        --output /jre
#----------------------------------------------------
FROM gradle:8.5-jdk17 AS build
COPY . /thred-server
COPY build.gradle settings.gradle /thred-server/
WORKDIR /thred-server
RUN ./gradlew clean build --build-cache -x test
#----------------------------------------------------
FROM alpine:3.18.4
ENV JAVA_HOME=/jre
ENV PATH="$JAVA_HOME/bin:$PATH"
COPY --from=builder-jre /jre $JAVA_HOME
COPY --from=build /thred-server/build/libs/*.jar thred-server.jar
ENTRYPOINT ["java", "-jar", "thred-server.jar"]
