FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp package

FROM tomcat:9.0-jdk21-temurin
ENV TZ=America/Lima
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /build/target/arquident.war /usr/local/tomcat/webapps/arquident.war
HEALTHCHECK --interval=10s --timeout=5s --start-period=60s --retries=12 \
  CMD curl --fail --silent http://127.0.0.1:8080/arquident/servicios > /dev/null || exit 1
EXPOSE 8080
