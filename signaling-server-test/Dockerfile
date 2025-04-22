FROM openjdk:21-slim-buster

WORKDIR /app

COPY ./build/libs .

ENTRYPOINT ["java", "-jar", "signaling-server-test.jar"]

EXPOSE 8080

