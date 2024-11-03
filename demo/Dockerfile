# Fase de construção
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Fase final
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar
COPY dados /app/dados
EXPOSE 8081 8082 8083

# Configura o modo de execução com base na variável de ambiente APP_MODE
ENTRYPOINT ["sh", "-c", "if [ \"$APP_MODE\" = 'server' ]; then java -jar app.jar --server.port=$SERVER_PORT; else java -jar app.jar --client.mode=true --server.url=$SERVER_URL; fi"]
