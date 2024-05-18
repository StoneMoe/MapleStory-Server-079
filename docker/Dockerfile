FROM openjdk:11-jdk as build

RUN sed -i 's/deb.debian.org/mirrors.ustc.edu.cn/g' /etc/apt/sources.list && \
    apt-get update && \
    apt-get install -y maven

WORKDIR /app
COPY . /app

RUN mkdir ~/.m2 -p && \
    cp /app/docker/config/maven.xml ~/.m2/settings.xml && \
    mvn clean package

FROM openjdk:11-jdk

WORKDIR /app
COPY --from=build /app/target/cms079-1.0-SNAPSHOT-jar-with-dependencies.jar /app/MapleStory_Server.jar
COPY --from=build /app/config /app/config
COPY --from=build /app/docker/config/db.properties /app/config/
COPY --from=build /app/docker/config/server.properties /app/config/
COPY --from=build /app/scripts /app/scripts
COPY --from=build /app/logs /app/logs
COPY --from=build /app/docs/ms_20210813_234816.sql /app/
COPY --from=build /app/start.sh /app/

RUN sed -i 's/deb.debian.org/mirrors.ustc.edu.cn/g' /etc/apt/sources.list && \
    apt update && \
    apt install -y mariadb-client

ENV MYSQL_USER=maplestory
ENV MYSQL_PASSWORD=maplestory
ENV MYSQL_DATABASE=maplestory
ENV MYSQL_HOST=db
ENV IP=127.0.0.1

ENTRYPOINT ["bash", "/app/start.sh"]