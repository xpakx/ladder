# Build stage
FROM maven:3.8.5-openjdk-11-slim AS build
WORKDIR /app
COPY . /app
RUN mvn clean package -DskipTests

# Run stage
FROM adoptopenjdk/openjdk11:ubi
MAINTAINER xpakx.github.io
EXPOSE 8080
COPY --from=build app/target/ladder-0.0.1-SNAPSHOT.jar application.jar
ENTRYPOINT ["java","-jar","/application.jar"]
