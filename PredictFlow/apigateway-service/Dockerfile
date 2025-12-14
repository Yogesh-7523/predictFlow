FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml . 
COPY apigateway-service/pom.xml apigateway-service/pom.xml
COPY apigateway-service/src apigateway-service/src
RUN mvn -B -f apigateway-service/pom.xml -DskipTests package spring-boot:repackage

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/apigateway-service/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]