# Multi-stage Dockerfile for Scanner API Service
# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy parent POM and scanner-common first (for better layer caching)
COPY pom.xml .
COPY scanner-common/pom.xml ./scanner-common/
COPY scanner-common/src ./scanner-common/src

# Copy scanner-api
COPY scanner-api/pom.xml ./scanner-api/
COPY scanner-api/src ./scanner-api/src

# Copy scanner-worker pom to satisfy reactor requirements
COPY scanner-worker/pom.xml ./scanner-worker/

# Build the application
# -pl: project list (build only scanner-api)
# -am: also make (build dependencies like scanner-common)
# -DskipTests: skip tests during Docker build (run separately)
RUN mvn clean package -pl scanner-api -am -DskipTests

# Stage 2: Runtime with minimal JRE
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/scanner-api/target/*.jar app.jar

# Expose port 8080 for the REST API
EXPOSE 8080

# Health check endpoint (Spring Boot Actuator)
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
