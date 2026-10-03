# Multi-stage build: compile with the full JDK/Maven, ship only the JRE + jar.
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /build

# Cache dependencies separately from source so `docker build` skips the
# ~/.m2 download on every source change.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline

COPY src/ src/
RUN ./mvnw -q -B package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Run as non-root.
RUN groupadd -r posterpro && useradd -r -g posterpro posterpro
COPY --from=build /build/target/*.jar app.jar
RUN chown posterpro:posterpro app.jar
RUN mkdir -p /app/logs && chown -R posterpro:posterpro /app
USER posterpro

# Actual port is PORT (application.yml: server.port: ${PORT:8080}) — set PORT
# if your platform assigns one dynamically (Railway/Render do this).
EXPOSE 8080

# SPRING_PROFILES_ACTIVE must be set at runtime (e.g. "prod") — there is no
# baked-in default, matching application.yml's fail-fast-if-unset design.
ENTRYPOINT ["java", "-jar", "app.jar"]
