# Stage 1: Build the project
FROM maven:3.9.1-eclipse-temurin-21 as build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the app
FROM openjdk:21-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 9095
ENTRYPOINT ["java", "-jar", "app.jar"]
