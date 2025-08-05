# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
# Copy only pom.xml first to leverage Docker cache for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline
# Now copy the rest of the source code
COPY . .
RUN mvn clean package -DskipTests -Dspring.profiles.active=docker

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/luckydraw-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]
