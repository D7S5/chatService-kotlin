FROM gradle:8.14.3-jdk17 AS build
WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew && ./gradlew clean bootJar -x test

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/app.jar /app/app.jar
EXPOSE 9090
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app/app.jar"]