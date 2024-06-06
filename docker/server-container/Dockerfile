FROM bellsoft/liberica-openjdk-alpine:17
ARG JAR_FILE=/docker/server-container/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=dev","app.jar"]
ENV TZ Asia/Seoul

