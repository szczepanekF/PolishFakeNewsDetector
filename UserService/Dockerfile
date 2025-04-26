FROM maven:3.9.9-amazoncorretto-24 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:24-oracle AS run
WORKDIR /app
COPY --from=build /app/target/UserService*.jar /app/user-service.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar","user-service.jar"]
