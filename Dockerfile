# Use Java 17
FROM eclipse-temurin:17-jdk-jammy

# Jar file copy
COPY target/*.jar app.jar

# Run app
ENTRYPOINT ["java","-jar","/app.jar"]