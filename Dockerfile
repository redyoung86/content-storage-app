FROM frolvlad/alpine-oraclejdk8:slim
MAINTAINER ayakurayuki AyakuraYuki@users.noreply.github.com

ENV WORK_DIR=/data/content_storage_app/
ENV JAR_FILE=target/content_storage_app-2.2.0.jar

VOLUME /$WORK_DIR/data/

ADD $JAR_FILE /$WORK_DIR/csa.jar
COPY data/storage.db /$WORK_DIR/data/
ADD src/main/resources/application.yml /$WORK_DIR/config/

WORKDIR $WORK_DIR
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","csa.jar"]