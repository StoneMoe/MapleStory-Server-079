FROM openjdk:11-jdk as build

RUN apt-get update && \
    apt-get install -y maven

WORKDIR /app
COPY . /app
RUN mvn clean package

FROM openjdk:11-jdk

WORKDIR /app
COPY --from=build /app/target/cms079-1.0-SNAPSHOT-jar-with-dependencies.jar /app/MapleStory_Server.jar
COPY --from=build /app/config /app/config
COPY --from=build /app/config/docker/db.properties /app/config/
COPY --from=build /app/config/docker/server.properties /app/config/
COPY --from=build /app/scripts /app/scripts
COPY --from=build /app/logs /app/logs
COPY --from=build /app/docs/ms_20210813_234816.sql /app/
COPY --from=build /app/start.sh /app/

RUN apt update && \
    apt install -y mariadb-client

ENV MYSQL_USER=maplestory
ENV MYSQL_PASSWORD=maplestory
ENV MYSQL_DATABASE=maplestory
ENV MYSQL_HOST=db
ENV IP=127.0.0.1

ENTRYPOINT ["bash", "/app/start.sh"]