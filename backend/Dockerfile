# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom first — Docker layer cache avoids re-downloading deps on code-only changes
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build the fat JAR (skip tests — run them in CI separately)
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Non-root user for security
RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring

# Copy the fat JAR from the build stage
COPY --from=builder /build/target/ai-resume-builder-*.jar app.jar

# Expose the application port
EXPOSE 8080

# JVM tuning for containers:
#   -XX:+UseContainerSupport      — respect cgroup CPU/memory limits
#   -XX:MaxRAMPercentage=75.0     — use 75% of container memory for heap
#   -Djava.security.egd=...       — faster SecureRandom startup (avoids /dev/random block)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
