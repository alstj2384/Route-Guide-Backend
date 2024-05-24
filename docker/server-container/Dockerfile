FROM openjdk:17
ARG JAR_FILE=/Docker/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar","app.jar"]
ENV TZ Asia/Seoul

