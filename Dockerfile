# ── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Cache dependencies first (faster rebuilds)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Build the jar
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=builder /app/target/candidate360-1.0.0.jar app.jar

RUN mkdir -p uploads/cv uploads/reports

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
