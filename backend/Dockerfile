# Etapa 1: compila o jar. Os testes ficam para o CI, então aqui pulamos.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /fonte
COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src src
RUN chmod +x mvnw && ./mvnw -B -DskipTests package

# Etapa 2: só o que precisa para rodar. O pg_dump entra porque o backup chama ele.
FROM eclipse-temurin:21-jre
# O Ubuntu da imagem não publica o pacote -16. O cliente 18 fala com o servidor 16.
RUN apt-get update \
    && apt-get install -y --no-install-recommends postgresql-client \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=build /fonte/target/sirtech-manager-*.jar app.jar
RUN mkdir -p /app/backups
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
