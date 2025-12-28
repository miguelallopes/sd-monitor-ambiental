# Stage 1: Build the application
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts* .
COPY settings.gradle.kts .
COPY gradle.properties .
COPY app app

# Make gradlew executable
RUN chmod +x gradlew

# Build the application (skip tests for faster build)
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
