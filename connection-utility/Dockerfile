FROM amazoncorretto:21-alpine3.21

WORKDIR /app

COPY build/distributions/connection-utility.zip /app

RUN unzip -q connection-utility.zip

WORKDIR /app/connection-utility/bin

RUN chmod +x ./connection-utility

ENTRYPOINT ["./connection-utility"]