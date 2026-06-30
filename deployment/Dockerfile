FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY mvnw pom.xml ./
COPY .mvn .mvn

RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src src

RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S unomi && adduser -S unomi -G unomi

WORKDIR /app

COPY --from=build /workspace/target/unomi-0.0.1-SNAPSHOT.jar /app/app.jar

USER unomi

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
