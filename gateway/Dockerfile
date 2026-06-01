# ============================================================
# API Gateway - Multi-stage Docker Build
# Build from repo root:
#   docker build -f gateway/Dockerfile -t ecommerce-gateway .
# ============================================================

# ---- Stage 1: Build ----
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy POM hierarchy first (leverage Docker layer caching)
COPY pom.xml .
COPY gateway/pom.xml gateway/
COPY shop-bff/pom.xml shop-bff/
COPY services/product-service/pom.xml services/product-service/
COPY services/user-service/pom.xml services/user-service/
COPY services/cart-service/pom.xml services/cart-service/
COPY services/order-service/pom.xml services/order-service/

# Use Alibaba Maven mirror (faster in China)
COPY settings.xml /root/.m2/settings.xml

# Install parent POM, then prefetch gateway dependencies
RUN mvn install -N -DskipTests -q \
 && mvn dependency:go-offline -pl gateway -DskipTests -q

# Copy source and build
COPY gateway/src gateway/src/
RUN mvn package -pl gateway -Dmaven.test.skip=true -q

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache curl \
 && addgroup -S app && adduser -S app -G app
USER app

WORKDIR /app

# Copy the fat JAR
COPY --from=build /app/gateway/target/*.jar app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -sf http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseG1GC", \
  "-XX:MaxRAMPercentage=75", \
  "-XX:+ExitOnOutOfMemoryError", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "/app/app.jar"]
