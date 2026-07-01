FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY deployment/spring-boot-admin/pom.xml ./pom.xml
COPY deployment/spring-boot-admin/src ./src

RUN apk add --no-cache maven && mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S unomi && adduser -S unomi -G unomi

WORKDIR /app

COPY --from=build /workspace/target/unomi-spring-boot-admin-0.0.1-SNAPSHOT.jar /app/app.jar

USER unomi

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
