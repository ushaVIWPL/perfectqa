# ==========================================
# STAGE 1: Build stage
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build final jar
COPY src ./src
RUN mvn package -DskipTests -B

# ==========================================
# STAGE 2: Runtime stage
# ==========================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install fontconfig and fonts required for OpenPDF / iText PDF generation in Linux container
RUN apk add --no-cache fontconfig ttf-dejavu

# Create directories for persistent uploads and logs
RUN mkdir -p uploads/logos uploads/screenshots logs

# Copy jar from build stage
COPY --from=builder /app/target/perfectqa.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# Run Spring Boot application
ENTRYPOINT ["java", "-Djava.awt.headless=true", "-jar", "app.jar"]
