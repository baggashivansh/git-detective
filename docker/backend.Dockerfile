FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven \
    && mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S gitdetective && adduser -S gitdetective -G gitdetective
USER gitdetective

COPY --from=build /workspace/target/git-detective-backend.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
