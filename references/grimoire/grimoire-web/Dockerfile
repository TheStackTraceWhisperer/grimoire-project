FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# Copy parent POM and all modules needed for the build
COPY pom.xml ./
COPY grimoire-shared ./grimoire-shared
COPY grimoire-data ./grimoire-data
COPY grimoire-web ./grimoire-web
COPY grimoire-server ./grimoire-server
COPY grimoire-client-v2 ./grimoire-client-v2
COPY grimoire-launcher ./grimoire-launcher

# Build the web application
RUN mvn -B -ntp clean package -pl grimoire-web -am -DskipTests

FROM azul/zulu-openjdk:25-jre-latest
WORKDIR /app

# Copy the built jar
COPY --from=build /app/grimoire-web/target/grimoire-web-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
