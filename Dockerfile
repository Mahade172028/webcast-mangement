FROM openjdk:17-alpine3.14
EXPOSE 8010
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=dev","-Djdk.tls.client.protocols=TLSv1.2", "-jar","/app.jar"]
