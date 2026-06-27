# ============================================================================
# Stage 1: BUILD
# ============================================================================
# Purpose: Compile and package the Spring Boot application
# Uses: Maven image with Java 21
# ============================================================================

FROM maven:3.9-eclipse-temurin-21 AS builder

# Set working directory
WORKDIR /app

# Copy Maven configuration files
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Cache Maven dependencies layer (separate from source code)
# This layer is reused if only source code changes
RUN mvn dependency:go-offline -B

# Copy source code
COPY src src

# Build the application (skip tests for faster build)
RUN mvn clean package -DskipTests -B

# ============================================================================
# Stage 2: RUNTIME
# ============================================================================
# Purpose: Run the compiled Spring Boot application
# Uses: Minimal Java 21 runtime image
# ============================================================================

FROM eclipse-temurin:21-jre-alpine

# Set metadata
LABEL maintainer="AI Order Reassignment Team"
LABEL description="AI-powered order reassignment engine - Backend service"
LABEL version="1.0"

# Create non-root user for security
RUN addgroup -g 1000 appuser && \
    adduser -u 1000 -G appuser -h /home/appuser -s /sbin/nologin -D appuser

# Set working directory
WORKDIR /home/appuser

# Copy compiled JAR from builder stage
COPY --from=builder /app/target/backend-*.jar app.jar
RUN chown -R appuser:appuser /home/appuser

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/orders || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

# ============================================================================
# Build Arguments & Defaults
# ============================================================================
# Override with: docker build --build-arg JAVA_OPTS="-Xmx512m"
# ============================================================================

ARG JAVA_OPTS=""
ENV JAVA_OPTS="${JAVA_OPTS:-Xmx256m -Xms128m}"
