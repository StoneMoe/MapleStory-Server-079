FROM mysql:5.7.42-oracle
RUN yum install  -y  net-tools vim
RUN curl -L https://mirrors.huaweicloud.com/java/jdk/7u80-b15/jdk-7u80-linux-x64.tar.gz -o /tmp/jdk-7u80-linux-x64.tar.gz
RUN tar -xf /tmp/jdk-7u80-linux-x64.tar.gz -C /usr/local

ARG  MYSQL_ROOT_PASSWORD
ARG  IP

ENV JAVA_HOME /usr/local/jdk1.7.0_80
ENV PATH $JAVA_HOME/bin:$PATH
ENV CLASSPATH  .:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar

RUN mkdir  /MapleStory-Server-079
ADD bin  /MapleStory-Server-079/bin
ADD config  /MapleStory-Server-079/config
ADD scripts  /MapleStory-Server-079/scripts
COPY start.sh /MapleStory-Server-079/
COPY ms_20210813_234816.sql /MapleStory-Server-079/

ADD UnlimitedJCEPolicy.tar /tmp
RUN mv  -f  /tmp/UnlimitedJCEPolicy/* /usr/local/jdk1.7.0_80/jre/lib/security/


RUN sed '430 a         nohup sh /MapleStory-Server-079/start.sh &' -i /usr/local/bin/docker-entrypoint.sh
