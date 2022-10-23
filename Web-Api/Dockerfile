#SpringBoot Dockerfile
FROM mcr.microsoft.com/java/jdk:11-zulu-ubuntu
ADD build/libs/Web-Api-1.0.0.jar app.jar
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]