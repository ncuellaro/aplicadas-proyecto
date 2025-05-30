# Usamos una imagen oficial de Java 17 con OpenJDK
FROM openjdk:21-slim

# Carpeta donde se copiará el jar
WORKDIR /app

# Copia el jar construido (ajusta el nombre si es diferente)
COPY target/*.jar app.jar

# Expone el puerto en el que corre Spring Boot (por defecto 8080)
EXPOSE 9095

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "app.jar"]
