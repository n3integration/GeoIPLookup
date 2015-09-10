from phusion/baseimage
MAINTAINER n3integration@gmail.com

RUN \
    apt-get update && \
    apt-get install -y openjdk-7-jre && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /usr/local/geoip

ENV JAVA_HOME /usr/lib/jvm/java-7-openjdk-amd64

EXPOSE 7070

WORKDIR /usr/local/geoip

COPY *.jar ./

CMD ["/usr/bin/java", "-jar", "/usr/local/geoip/geoip.jar"]

