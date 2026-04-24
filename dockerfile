# Stage 1 – build application
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace/app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2 – extract layers
FROM eclipse-temurin:17-jre AS extract
WORKDIR /workspace/app
COPY --from=build /workspace/app/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 3 – final image
FROM eclipse-temurin:17-jre
WORKDIR /workspace/app
COPY --from=extract /workspace/app/dependencies/ ./
COPY --from=extract /workspace/app/spring-boot-loader/ ./
COPY --from=extract /workspace/app/snapshot-dependencies/ ./
COPY --from=extract /workspace/app/application/ ./
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
