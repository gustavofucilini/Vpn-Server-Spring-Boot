# Utiliza uma imagem base do Maven para compilar e construir o projeto
FROM maven:3.9.8-eclipse-temurin-22 AS build

# Define o diretório de trabalho
WORKDIR /app

# Copia todo o conteúdo do projeto para o diretório de trabalho
COPY . .

RUN mvn install:install-file -Dfile=libs/tuntap4j-1.0.0.jar -DgroupId=one.papachi -DartifactId=tuntap4j -Dversion=1.0.0 -Dpackaging=jar

# Faz o download das dependências
RUN mvn dependency:go-offline

# Compila e empacota a aplicação
RUN mvn clean install package -DskipTests -X

# Utiliza uma imagem base do JDK para rodar a aplicação
FROM eclipse-temurin:22-jdk

# Define o diretório de trabalho
WORKDIR /app

# Copia o JAR gerado no estágio anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta que a aplicação irá utilizar
EXPOSE 9091 9090

# Define o comando padrão para executar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]