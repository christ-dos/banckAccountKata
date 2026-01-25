FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=build /app/target/bank-account-*.jar app.jar
RUN chown spring:spring  app.jar
USER spring:spring
EXPOSE 9090
ENTRYPOINT ["java","-jar","app.jar"]