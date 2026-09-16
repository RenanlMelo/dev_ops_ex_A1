# ---- Build stage ----
FROM eclipse-temurin:24-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline

COPY src ./src
RUN ./mvnw -q -o -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:24-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
VOLUME ["/app/data"]

ENTRYPOINT ["java", "-jar", "app.jar"]
