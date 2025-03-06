FROM openjdk:21-slim-buster

WORKDIR /app

COPY ./build/libs .

ENTRYPOINT ["java", "-jar", "turn-server.jar"]

EXPOSE 3478/udp
EXPOSE 3479/udp

