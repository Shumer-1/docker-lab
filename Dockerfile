FROM openjdk:17-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

COPY target/docker-1.0-SNAPSHOT.jar app.jar

RUN rm -rf /var/cache/apk/*

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
