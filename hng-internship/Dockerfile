FROM openjdk:17-jdk-slim
LABEL maintainer="chukwumaemmanuella03@gmail.com"
WORKDIR /app
COPY target/hng-internship-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT [ "java", "-jar", "app.jar" ]