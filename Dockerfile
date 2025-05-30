# Etapa 1: Construcción del proyecto
FROM maven:3.9.4-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src /app/src

RUN mvn clean package -DskipTests

# Etapa 2: Imagen final para ejecutar la aplicación
FROM openjdk:21-slim

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 9095

ENTRYPOINT ["java", "-jar", "app.jar"]
