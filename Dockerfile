FROM eclipse-temurin:20_36-jdk-alpine as deps

# Identify dependencies
COPY ./target/*.jar /app/app.jar
RUN mkdir /app/unpacked && \
    cd /app/unpacked && \
    unzip ../app.jar && \
    cd .. && \
    $JAVA_HOME/bin/jdeps \
    --ignore-missing-deps \
    --print-module-deps \
    -q \
    --recursive \
    --multi-release 19 \
    --class-path="./unpacked/BOOT-INF/lib/*" \
    --module-path="./unpacked/BOOT-INF/lib/*" \
    ./app.jar > /deps.info

# base image to build a JRE
FROM eclipse-temurin:20_36-jdk-alpine as temurin-jdk

# required for strip-debug to work
RUN apk add --no-cache binutils

# copy module dependencies info
COPY --from=deps /deps.info /deps.info

# Build small JRE image
RUN $JAVA_HOME/bin/jlink \
    --verbose \
    --add-modules $(cat /deps.info) \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /customjre

FROM alpine:latest
ENV JAVA_HOME=/jre
ENV PATH="${JAVA_HOME}/bin:${PATH}"

# copy JRE from the base image
COPY --from=temurin-jdk /customjre $JAVA_HOME

# Add app user
ARG APPLICATION_USER=app
RUN adduser --no-create-home -u 1000 -D $APPLICATION_USER

# Configure working directory
RUN mkdir /app && \
    chown -R $APPLICATION_USER /app

USER 1000

COPY --chown=1000:1000 ./target/*.jar /app/app.jar
WORKDIR /app

EXPOSE 8080
ENTRYPOINT [ "/jre/bin/java", "-jar", "/app/app.jar" ]
