FROM maven:3.9.9-amazoncorretto-24 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:24-oracle AS run
WORKDIR /app
COPY --from=build /app/target/BusinessLogicService*.jar /app/business-logic-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar","business-logic-service.jar"]
CMD []