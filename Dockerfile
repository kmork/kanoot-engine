# Use Gradle to build the application
FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle build --no-daemon

# Use a distroless base image for the final build
FROM gcr.io/distroless/java21-debian12
WORKDIR /app
COPY --from=build /app/build/libs/kanoot-0.0.1.jar app.jar

# Run the JAR file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

## example below from sykepenger

#FROM gcr.io/distroless/java21-debian12:nonroot

#ENV TZ="Europe/Oslo"
#ENV JAVA_OPTS='-XX:MaxRAMPercentage=90'

#WORKDIR /app

#COPY spinnvill-app/build/libs/*.jar .

#CMD ["app.jar"]
