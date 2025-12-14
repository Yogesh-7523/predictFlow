FROM maven:3.9.4-eclipse-temurin-17 as builder
WORKDIR /build
# include parent pom (project root) and module sources
COPY pom.xml .
COPY transaction-service/pom.xml transaction-service/pom.xml
COPY transaction-service/src transaction-service/src
RUN mvn -B -f transaction-service/pom.xml -DskipTests package spring-boot:repackage

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/transaction-service/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]