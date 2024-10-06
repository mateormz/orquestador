# Usa una imagen base de Java
FROM openjdk:17-jdk-slim

# Configura el directorio de trabajo
WORKDIR /app

# Copia el JAR construido a la imagen
COPY target/orquestador-0.0.1-SNAPSHOT.jar app.jar

# Expone el puerto que tu aplicación usa
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]