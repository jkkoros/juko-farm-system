# Use Eclipse Temurin (Adoptium) OpenJDK 17 - stable and recommended for Spring Boot
FROM eclipse-temurin:17-jdk

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

# Build the app (skip tests to speed up)
RUN ./mvnw clean package -DskipTests

# Expose your app's port (change 8085 if different)
EXPOSE 8085

# Run the JAR file (adjust if your JAR has a specific name)
CMD ["java", "-jar", "target/*.jar"]