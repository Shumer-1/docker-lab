# Лабораторная работа "Docker: докеризация приложения"

## Выполнение работы

1) Для выполнения задания было написано простое приложение на Spring, которое выводит на страницу список пользователей из таблицы myUser, для работы с базой
данных используется Hibernate (косвенно через Spring Data JPA). Есть контроллер для добавления новых пользователей и демонстрации текущих (Добавление осуществлялось через curl). 
Файлы с кодом приложения находятся в com.example.application. 
2) В директории resources находится файл с кодом миграции (папка db.migration). Данный файл просто создает нужную таблицу в базе данных.
3) В директории resources также находится файл application.properties:
```
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

spring.jpa.hibernate.ddl-auto=${SPRING_JPA_HIBERNATE_DDL_AUTO}
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

server.port=8080
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.r2dbc.R2dbcDataAutoConfiguration
spring.r2dbc.enabled=false
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```
Здесь происходит настройка подключения к базе данных (для этого используются переменные окружения), выбирается поведение Hibernate (update/validate). Кроме этого, указывается порт,
диалект SQL, а также осуществляется настройка миграции.
3) В корневой директории проекта лежит Dockerfile:
```
FROM openjdk:17-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app

COPY target/docker-1.0-SNAPSHOT.jar app.jar

RUN rm -rf /var/cache/apk/*

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```
В этом файле указывается базовый образ (alpine), создается новый пользователь и группа внутри контейнера (теперь приложение будет работать от имени 
непривилегированного пользователя, а не от root). Происходит переключение на этого пользователя. Устанавливается рабочая директория (та, в которой находится приложение).
Также здесь копируется исполняемый файл, очищается кэш.

5) В корневой директории находится и docker-compose.yml файл:
```
version: '3'
services:
  db:
    image: postgres:15
    environment:
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      POSTGRES_DB: ${POSTGRES_DB}
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - docker_network

  app:
    image: yourapp:latest
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - db
    networks:
      - docker_network
    environment:
      SPRING_DATASOURCE_URL: ${SPRING_DATASOURCE_URL}
      SPRING_DATASOURCE_USERNAME: ${SPRING_DATASOURCE_USERNAME}
      SPRING_DATASOURCE_PASSWORD: ${SPRING_DATASOURCE_PASSWORD}
      SPRING_JPA_HIBERNATE_DDL_AUTO: ${SPRING_JPA_HIBERNATE_DDL_AUTO}

volumes:
  postgres-data:
    driver: local

networks:
  docker_network:
    driver: bridge
```
В этом файле описываются два сервиса: db (для базы данных PostgreSQL) и app (для самого приложения).
Рассмотрим их по порядку:
1. db
   Здесь устанавливается использование Docker-образа PostgreSQL 15 для запуска базы данных, задаются переменные окружения для
   контейнера базы данных (значения берутся из файла .env), открывается порт 5432 на хосте и пробрасывается в контейнер (также на порт 5432).
   Создается и монтируется volume с именем postgres-data, которое сохраняет данные базы данных.
2. app
   Этот сервис представляет собой контейнер, который запускает само приложение. Он использует образ,
   который собирается из текущей директории с помощью Dockerfile. Контейнер пробрасывает порт 8080,
   чтобы приложение было доступно извне. С помощью параметра depends_on он зависит от запуска контейнера с базой данных db.
   Это нужно, чтобы гарантировать, что база данных будет запущена перед приложением (иначе возникнут ошибки подключения).
   Здесь также считываются переменнные окружения. Приложение подключается к сети docker_network, что позволяет ему взаимодействовать с
   другими сервисами в пределах этой сети.
Кроме сервисов, еще в networks используется bridge, чтобы контейнеры могли общаться друг с другом напрямую.

6) Команды для запуска:
    mvn clean package - сборка приложения.
    docker-compose up --build - запуск докер контейнеров (и сборка образов).

7) Пример работы с приложением:
   ...$ mvn clean package
   ...$ docker-compose up --build
   ...$ curl -X GET http://localhost:8080/users -H "Content-Type: application/json"
 
   [{"id":1,"name":"John Doe"},{"id":2,"name":"New User"},{"id":3,"name":"Third User"}]

   ...$ curl -X POST http://localhost:8080/users -H "Content-Type: application/json" -d '{"name": "Last User"}'

   {"id":4,"name":"Last User"}

   ...$ curl -X GET http://localhost:8080/users -H "Content-Type: application/json"

   [{"id":1,"name":"John Doe"},{"id":2,"name":"New User"},{"id":3,"name":"Third User"},{"id":4,"name":"Last User"}]
