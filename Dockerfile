# Use a base image with both Java 21 and Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set the working directory
WORKDIR /app

# Copy the pom.xml first to cache dependencies
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy the source code
COPY . .

# Build the application with docker profile
RUN mvn clean install -P docker -Dmaven.test.skip=true

# Use a smaller JRE-only image for runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built jar
COPY --from=builder /app/target/margdarshak-0.0.1-SNAPSHOT.jar /app/margdarshak.jar

# Expose the application port
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "margdarshak.jar"]

