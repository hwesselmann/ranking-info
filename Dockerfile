FROM eclipse-temurin:25-jdk-jammy AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src src
RUN ./mvnw package -DskipTests --no-transfer-progress

ARG NEW_RELIC_AGENT_VERSION=9.3.0
RUN apt-get update -q && apt-get install -y -q --no-install-recommends unzip curl \
    && curl -fsSL "https://download.newrelic.com/newrelic/java-agent/newrelic-agent/${NEW_RELIC_AGENT_VERSION}/newrelic-java-${NEW_RELIC_AGENT_VERSION}.zip" \
       -o newrelic-java.zip \
    && unzip -q newrelic-java.zip \
    && rm newrelic-java.zip \
    && apt-get purge -y --auto-remove curl unzip \
    && rm -rf /var/lib/apt/lists/*

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN adduser -D -s /bin/false appuser
COPY --from=builder /app/target/*.jar app.jar
COPY --from=builder /app/newrelic/newrelic.jar newrelic/newrelic.jar
COPY newrelic/newrelic.yml newrelic/newrelic.yml
RUN chown -R appuser:appuser app.jar newrelic/
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", \
  "-javaagent:/app/newrelic/newrelic.jar", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseZGC", \
  "-XX:+ZGenerational", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-jar", "app.jar"]
