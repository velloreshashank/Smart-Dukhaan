# Multi-stage build for KiranaLink Spring Boot Backend
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production jar
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root system user for security
RUN addgroup -S kirana && adduser -S kirana -G kirana
USER kirana

# Copy repackaged fat jar
COPY --from=builder /app/target/kiranalink-backend-1.0.0.jar app.jar

EXPOSE 5001

ENV PORT=5001 \
    DB_HOST=mysql \
    DB_PORT=3306 \
    DB_NAME=kirana_link \
    DB_USER=root \
    DB_PASSWORD=root

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
