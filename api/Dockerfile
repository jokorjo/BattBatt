FROM maven:3.9.6-eclipse-temurin-17

WORKDIR /app
COPY . .

RUN mvn clean package

CMD ["java", "-jar", "target/battbatt-0.0.1-SNAPSHOT.jar"]
