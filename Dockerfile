FROM eclipse-temurin:25-jdk-jammy AS builder
WORKDIR /app
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q
COPY src src
RUN ./mvnw package -DskipTests --no-transfer-progress

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN adduser -D -s /bin/false appuser
COPY --from=builder /app/target/*.jar app.jar
RUN chown appuser:appuser app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:+UseZGC", \
  "-XX:+ZGenerational", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-jar", "app.jar"]
