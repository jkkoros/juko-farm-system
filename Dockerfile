# Use Eclipse Temurin OpenJDK 17
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml ./
COPY mvnw ./
COPY .mvn ./.mvn

# Make mvnw executable
RUN chmod +x ./mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source
COPY src ./src

# Build the app
RUN ./mvnw clean package -DskipTests

# Expose port (your app uses 8085)
EXPOSE 8085

# Run the EXACT JAR name (from your log)
CMD ["java", "-jar", "target/JukoFarm-Management-0.0.1-SNAPSHOT.jar"]