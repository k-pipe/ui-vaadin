# ---- Build Stage ----
FROM gradle:8.10-jdk17 AS build

WORKDIR /app

# Copy Gradle wrapper
COPY gradlew .
COPY gradle ./gradle

# Copy build files
COPY build.gradle settings.gradle gradle.properties ./

# Copy sources
COPY src ./src

# Make gradlew executable
RUN chmod +x ./gradlew

# Build for production
RUN ./gradlew -Pvaadin.productionMode build -x test --no-daemon

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app

# Copy built Quarkus app
COPY --from=build /app/build/quarkus-app /app

EXPOSE 8080

CMD ["java", "-jar", "quarkus-run.jar"]