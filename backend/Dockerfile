# ---- Build stage ----
FROM eclipse-temurin:24-jdk AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
# Retry interrupted downloads without hiding a persistent resolution failure.
RUN chmod +x mvnw && \
    for attempt in 1 2 3; do \
        ./mvnw -B -U dependency:go-offline && break; \
        if [ "$attempt" -eq 3 ]; then exit 1; fi; \
        echo "Dependency download failed; retrying in 10 seconds ($attempt/3)"; \
        sleep 10; \
    done

COPY src ./src
RUN ./mvnw -B -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:24-jre
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
VOLUME ["/app/data"]

HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
