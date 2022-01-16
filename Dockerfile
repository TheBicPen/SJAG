
# Build image
FROM maven:3.6.3-openjdk-16 AS Build
COPY src /src
COPY pom.xml /
RUN mvn clean package

# Execution image
FROM openjdk:16
COPY --from=Build /target/ApiGateway-1.0.jar /
ENTRYPOINT [ "java", "-jar", "/ApiGateway-1.0.jar" ]
EXPOSE 8000