# Dockerfile
FROM maven:3.9.6-eclipse-temurin-21-alpine

WORKDIR /app

COPY . /app

RUN apk add --no-cache libc6-compat && \
    echo "export LANG=en_US.UTF-8" >> /etc/profile
ENV LANG en_US.UTF-8

RUN ./mvnw package -DskipTests

EXPOSE 8080

CMD ["java", "-jar", "target/S5.02.-Mascota-Virtual-0.0.1-SNAPSHOT.jar"]