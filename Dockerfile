FROM adoptopenjdk/openjdk15:alpine-jre
WORKDIR /opt/app
ARG JAR_FILE=target/demo-0.0.1.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]
