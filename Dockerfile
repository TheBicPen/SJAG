
# Build image
FROM maven:3 AS Build
COPY src /src
COPY pom.xml /
RUN mvn clean package

# Execution image
FROM openjdk:17
COPY --from=Build /target/ApiGateway-0.1.jar /
ENTRYPOINT [ "java", "-jar", "/ApiGateway-0.1.jar" ]
EXPOSE 8000