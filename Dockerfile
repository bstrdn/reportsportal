FROM adoptopenjdk/openjdk15:alpine-jre
WORKDIR /opt/app
ARG JAR_FILE=target/report-0.3.5.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","app.jar"]
#docker build -t report-0.3.5.jar .
#java -jar report-0.3.5.jar