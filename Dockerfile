FROM openjdk:11-jdk-slim
ADD /build/libs/*.jar app.jar
ENTRYPOINT ["java"