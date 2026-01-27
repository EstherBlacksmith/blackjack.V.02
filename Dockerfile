# Multi-stage build for Spring Boot application
# Stage 1: Build stage with Java 21 and Maven
FROM eclipse-temurin:21-jdk-alpine AS builder

# Install Maven
RUN apk add --no-cache maven

WORKDIR /app
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage with Java 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/blackjack-0.0.1-SNAPSHOT.jar app.jar

# Expose application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
