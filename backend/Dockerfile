FROM maven:3.9.9-eclipse-temurin-17 AS build
ARG SERVICE_NAME
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests -pl backend/${SERVICE_NAME} -am package

FROM eclipse-temurin:17-jre
ARG SERVICE_NAME
ENV JAVA_OPTS=""
WORKDIR /app
COPY --from=build /workspace/backend/${SERVICE_NAME}/target/${SERVICE_NAME}-0.1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
