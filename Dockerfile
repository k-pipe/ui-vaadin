# ---- Build Stage ----
FROM gradle:8.10-jdk17 AS build

WORKDIR /app

# Copy build files first (better caching)
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle

# Download dependencies
RUN gradle build -x test --no-daemon || return 0

# Copy the source
COPY src ./src

# Build for production (Vaadin + Quarkus)
RUN ./gradlew -Pvaadin.productionMode build -x test --no-daemon

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jre AS runtime

WORKDIR /app

# Copy built Quarkus app
COPY --from=build /app/build/quarkus-app /app

EXPOSE 8080

CMD ["java", "-jar", "quarkus-run.jar"]