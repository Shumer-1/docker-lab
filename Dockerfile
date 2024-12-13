# Используем легковесный базовый образ Java
FROM openjdk:17-alpine

# Создаем непривилегированного пользователя
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем сборочный файл
COPY target/docker-1.0-SNAPSHOT.jar app.jar

# Устанавливаем переменные окружения для конфигурации
ENV JAVA_OPTS=""
ENV SPRING_DATASOURCE_URL=""
ENV SPRING_DATASOURCE_USERNAME=""
ENV SPRING_DATASOURCE_PASSWORD=""

# Запуск приложения
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
