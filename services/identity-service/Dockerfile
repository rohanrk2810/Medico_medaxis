FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/identity-service-0.1.0-SNAPSHOT.jar app.jar
USER 10001
ENTRYPOINT ["java","-jar","app.jar"]
