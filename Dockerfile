# ============================================================
# Multi-stage Dockerfile for all e-commerce backend services
# Usage:
#   docker build \
#     --build-arg MODULE=<module-path> \
#     --build-arg JAR_FILE=<jar-filename> \
#     -t <image-name> \
#     -f Dockerfile .
#
#   Module paths (from parent POM):
#     gateway, shop-bff,
#     services/product-service, services/user-service,
#     services/cart-service, services/order-service
# ============================================================

# ── Stage 1: Maven Build ─────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS build

ARG MODULE

WORKDIR /build

# Copy POM hierarchy first for isolated dependency layer
COPY pom.xml .
COPY gateway/pom.xml     gateway/
COPY shop-bff/pom.xml    shop-bff/
COPY services/product-service/pom.xml services/product-service/
COPY services/user-service/pom.xml    services/user-service/
COPY services/cart-service/pom.xml    services/cart-service/
COPY services/order-service/pom.xml   services/order-service/

# Use Alibaba Maven mirror (faster in China)
COPY settings.xml /root/.m2/settings.xml

# Install parent POM first, then pre-fetch dependencies
RUN mvn install -N -DskipTests -B -q \
 && mvn dependency:go-offline -pl ${MODULE} -am -B -q || true

# Copy all source code
COPY gateway/src/     gateway/src/
COPY shop-bff/src/    shop-bff/src/
COPY services/product-service/src/ services/product-service/src/
COPY services/user-service/src/    services/user-service/src/
COPY services/cart-service/src/    services/cart-service/src/
COPY services/order-service/src/   services/order-service/src/

# Build the target module (skip tests for CI speed)
RUN mvn package -pl ${MODULE} -am -DskipTests -B -q

# Extract the fat JAR to a known location
RUN find /build/${MODULE}/target -name '*.jar' ! -name '*-sources.jar' ! -name '*-javadoc.jar' \
      -exec cp {} /build/app.jar \; -quit

# ── Stage 2: Runtime ────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

LABEL org.opencontainers.image.source="https://github.com/ecommerce"
LABEL org.opencontainers.image.description="E-Commerce Platform Service"

WORKDIR /app

# Install curl for health check (Alpine doesn't include wget by default)
RUN apk add --no-cache curl

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the fat JAR
COPY --from=build /build/app.jar app.jar

# Switch to non-root
USER appuser

# Health check — uses Spring Boot Actuator
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
    CMD curl -sf http://localhost:${SERVER_PORT:-8080}/actuator/health || exit 1

ENTRYPOINT ["java", \
    "-XX:+UseG1GC", \
    "-XX:MaxRAMPercentage=75.0", \
    "-XX:+ExitOnOutOfMemoryError", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
