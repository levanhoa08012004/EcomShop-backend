# Base image
FROM eclipse-temurin:17
WORKDIR /app

# Copy file JAR vào container
COPY target/app.jar app.jar

# Expose port Spring Boot
EXPOSE 8080

# Chạy Spring Boot
ENTRYPOINT ["java","-jar","app.jar"]
