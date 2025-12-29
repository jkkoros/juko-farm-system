# Use Eclipse Temurin OpenJDK 17 - stable and recommended
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy Maven config files
COPY pom.xml ./
COPY mvnw ./
COPY .mvn ./.mvn

# Make mvnw executable (this is the key fix)
RUN chmod +x ./mvnw

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the app
RUN ./mvnw clean package -DskipTests

# Expose port (your app uses 8085)
EXPOSE 8085

# Run the JAR (adjust if your JAR name is specific)
CMD ["java", "-jar", "target/*.jar"]