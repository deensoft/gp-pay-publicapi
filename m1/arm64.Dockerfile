FROM eclipse-temurin:11-jre@sha256:b0081c873dc2f286627e1b0d15a93e170ca7c777f4a16b6fc1ac1420c09661f5

RUN ["apt-get", "update"]

ARG DNS_TTL=15

# Default to UTF-8 file.encoding
ENV LANG C.UTF-8

RUN echo networkaddress.cache.ttl=$DNS_TTL >> "$JAVA_HOME/conf/security/java.security"

RUN ["apt-get", "install", "-y", "bash", "tini"]

ENV PORT 8080
ENV ADMIN_PORT 8081

EXPOSE 8080
EXPOSE 8081

WORKDIR /app

COPY docker-startup.sh /app/docker-startup.sh
COPY target/*.yaml /app/
COPY target/pay-*-allinone.jar /app/

ENTRYPOINT ["tini", "-e", "143", "--"]

CMD ["bash", "./docker-startup.sh"]
