FROM eclipse-temurin:26-jdk AS build

WORKDIR /build
COPY mvnw .
RUN chmod +x mvnw
COPY .mvn .mvn
COPY pom.xml .

COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B package -DskipTests



FROM eclipse-temurin:26-jre

WORKDIR /app
COPY --from=build /build/target/*.jar app.jar

RUN useradd --system --uid 1001 --no-create-home appuser
USER 1001

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]