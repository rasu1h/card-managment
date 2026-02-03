# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build

COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
