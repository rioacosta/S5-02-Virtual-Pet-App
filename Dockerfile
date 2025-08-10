# Dockerfile
FROM maven:3.9.6-eclipse-temurin-21-alpine

WORKDIR /app

COPY . /app

RUN ./mvnw package -DskipTests

EXPOSE 8080

CMD ["sh", "-c", "java -jar target/Meditation-Buddys-0.0.1-SNAPSHOT.jar --server.port=${PORT:-8080}"]
