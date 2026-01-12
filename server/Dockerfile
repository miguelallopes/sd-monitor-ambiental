FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY gradle.properties .
COPY build.gradle.kts* .

COPY app/build.gradle.kts app/

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon
COPY app/src app/src

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/app/build/libs/*.jar app.jar

EXPOSE 50051

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
