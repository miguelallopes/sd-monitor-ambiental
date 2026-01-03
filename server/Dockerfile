# Stage 1: Build the application
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# --- Dependency Caching Layer ---

# 1. Copy Gradle wrapper and project settings
COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY gradle.properties .
COPY build.gradle.kts* .

# 2. Copy only the module-specific build file
# (This allows Gradle to resolve dependencies for 'app' without needing the source code yet)
COPY app/build.gradle.kts app/

# 3. Make gradlew executable and download dependencies
# This command runs ONLY if dependency files change. Otherwise, it uses the Docker cache.
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# --- Source Build Layer ---

# 4. Copy the actual source code
# If you change code in /app/src, only the steps after this line are re-run.
COPY app/src app/src


# 5. Build the application
RUN ./gradlew bootJar --no-daemon

# Stage 2: Run the application
FROM eclipse-temurin:25-jre

WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/app/build/libs/*.jar app.jar

# Expose gRPC port
EXPOSE 50051

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]