# Stage 1: Build
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Install dos2unix for gradlew script compatibility
RUN apt-get update && apt-get install --no-install-recommends -y dos2unix && rm -rf /var/lib/apt/lists/*

COPY gradlew ./
COPY gradle ./gradle
RUN chmod +x ./gradlew && dos2unix ./gradlew

# Cache dependencies based on build files
COPY build.gradle settings.gradle ./
RUN ./gradlew dependencies --no-daemon || echo "Dependency cache layer"

# Copy source and build JAR
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create non-root user for security
RUN groupadd --system appgroup && \
    useradd --system --gid appgroup appuser && \
    chown -R appuser:appgroup /app

# Copy artifact from builder stage
ARG JAR_FILE=/app/build/libs/sportclub-challenge-*.jar
COPY --from=builder ${JAR_FILE} app.jar
RUN chown appuser:appgroup app.jar

# Run as non-root user
USER appuser

# Expose application port
EXPOSE 8080

# Set runtime entrypoint with JVM options
ENTRYPOINT ["java", \
            "-XX:+UseZGC", \
            "-Xms256m", \
            "-Xmx512m", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", \
            "/app/app.jar"]