# ============================
# 1. Build-Stage
# ============================
FROM maven:3.9.8-eclipse-temurin-21 AS build

# Arbeitsverzeichnis setzen
WORKDIR /build

# Projektdateien kopieren
COPY pom.xml .
COPY src ./src

# Quarkus App bauen (Uber-Jar oder Fast-Jar)
RUN mvn -B clean install -Pproduction -DskipTests

# ============================
# 2. Run-Stage (nur das fertige JAR)
# ============================
FROM eclipse-temurin:21-jre-jammy

# Quarkus benötigt ein Arbeitsverzeichnis
WORKDIR /app

# aus Build-Stage kopieren (Fast-Jar Struktur von Quarkus)
COPY --from=build /build/target/quarkus-app /app/

# Exponiere Standardport
EXPOSE 8080

# Startkommando
ENTRYPOINT ["java", "-jar", "/app/quarkus-run.jar"]