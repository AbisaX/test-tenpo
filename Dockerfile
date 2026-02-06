# ===========================================
# Dockerfile - API Calculadora Tenpo
# Autor: Abisaid Gomez
# ===========================================

# Etapa de construccion
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar archivo de dependencias y descargarlas (cache de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar codigo fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa de ejecucion
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario no-root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiar el JAR compilado
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto de la aplicacion
EXPOSE 8080

# Verificacion de salud del contenedor
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Comando para ejecutar la aplicacion
ENTRYPOINT ["java", "-jar", "app.jar"]
