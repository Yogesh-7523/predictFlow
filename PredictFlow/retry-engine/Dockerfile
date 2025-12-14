FROM maven:3.9.4-eclipse-temurin-17 as builder
WORKDIR /build
COPY pom.xml .
COPY retry-engine/pom.xml retry-engine/pom.xml
COPY retry-engine/src retry-engine/src
RUN mvn -B -f retry-engine/pom.xml -DskipTests package spring-boot:repackage

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/retry-engine/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8083
ENTRYPOINT ["java","-jar","/app/app.jar"]