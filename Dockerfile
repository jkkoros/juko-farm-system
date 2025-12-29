# Use official OpenJDK 17 (good for most Spring Boot apps)
FROM openjdk:17-jdk-slim

# Set working directory inside the container
WORKDIR /app

# Copy Maven files first (for caching dependencies)
COPY pom.xml ./
COPY mvnw ./
COPY .mvn ./.mvn

# Download dependencies (this layer caches well)
RUN ./mvnw dependency:go-offline -B

# Copy the rest of the source code
COPY src ./src

# Build the app (skip tests to speed up build)
RUN ./mvnw clean package -DskipTests

# Expose the port your app uses (change 8085 if different)
EXPOSE 8085

# Run the JAR file
CMD ["java", "-jar", "target/*.jar"]