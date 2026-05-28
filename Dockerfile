# ============================================
# MULTI-STAGE BUILD - LIBRARY MANAGEMENT SYSTEM
# ============================================

# ---------- STAGE 1: BUILD ----------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy maven files first (for layer caching)
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (cached if pom.xml unchanged)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests -B

# Extract the JAR for layered build
RUN java -Djarmode=layertools -jar target/*.jar extract --destination extracted

# ---------- STAGE 2: RUNTIME ----------
FROM eclipse-temurin:21-jre-alpine

# Install useful tools
RUN apk add --no-cache curl tzdata

# Create non-root user
RUN addgroup -S library && adduser -S library -G library

# Set timezone
ENV TZ=Asia/Kolkata

WORKDIR /app

# Copy from builder (layered for optimal caching)
COPY --from=builder /build/extracted/dependencies/ ./
COPY --from=builder /build/extracted/spring-boot-loader/ ./
COPY --from=builder /build/extracted/snapshot-dependencies/ ./
COPY --from=builder /build/extracted/application/ ./

# Create upload directory
RUN mkdir -p /app/uploads && chown -R library:library /app

# Switch to non-root user
USER library

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:InitialRAMPercentage=25.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -Dspring.threads.virtual.enabled=true"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]