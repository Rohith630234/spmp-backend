# Stage 1: Build using Maven and Java 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy project files and build jar
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Lightweight Java 21 runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy built application jar
COPY --from=build /app/target/*.jar app.jar

# Expose default port
EXPOSE 8080

# Launch Spring Boot with dynamic Render port support
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
