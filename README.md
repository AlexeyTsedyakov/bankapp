# bankapp

Микросервисное демо-приложение «Банк» — это приложение с веб-интерфейсом, которое позволяет пользователю (клиенту
банка):

* регистрироваться в системе по логину и паролю (заводить аккаунт);
* добавлять счета в различных валютах;
* класть виртуальные деньги на счёт и снимать их;
* переводить деньги между своими счетами с учётом конвертации в различные валюты;
* переводить деньги на другой счёт с учётом конвертации в различные валюты;
* получать уведомления по email: проверить ящик можно по адресу http://localhost:8025 (Mailpit).

Приложение состоит из следующих микросервисов:

* фронта (Front UI);
* сервиса аккаунтов (Accounts);
* сервиса обналичивания денег (Cash);
* сервиса перевода денег между счетами одного или двух аккаунтов (Transfer);
* сервиса конвертации валют (Exchange);
* сервиса генерации курсов валют (Exchange Generator);
* сервиса блокировки подозрительных операций (Blocker);
* сервиса уведомлений (Notifications).

Java, Spring Boot, Spring Web MVC, Spring Security, Spring Data JDBC, Spring Cloud (Gateway, Consul),
Thymeleaf, HTMX, Bulma, Postgres, Flyway, Keycloak, Consul, Mailpit, Docker

## Требования

* Java 25+
* Postgres 18+
* Keycloak 26+
* Consul 2.1+
* Mailpit 1.32+

Либо

* Docker

## Как собирать

1. Создать `.env` файл из шаблона [.env.example](.env.example) в корне проекта. Значения по-умолчанию можно оставить
2. `./gradlew clean build`

## Как запустить с помощью Docker

1. [Собрать приложение](#как-собирать)
2. `docker-compose up`
3. Перейти на `http://localhost:8080`

## Как запустить локально

1. [Собрать проект](#как-собирать)
2. Установить Postgres и создать базу `bankapp`
3. Установить Keycloak и импортировать realm из файла [realm-export.json](keycloak/realm-export.json)
4. Установить Consul
5. Установить Mailpit
6. Параметры подключений установить в `.env` файле
7. Запустить модули приложения:

* `./gradlew :accounts:bootRun`
* `./gradlew :api-gateway:bootRun`
* `./gradlew :blocker:bootRun`
* `./gradlew :cash:bootRun`
* `./gradlew :exchange:bootRun`
* `./gradlew :exchange-generator:bootRun`
* `./gradlew :front-ui:bootRun`
* `./gradlew :notifications:bootRun`
* `./gradlew :transfer:bootRun`

8. Перейти на `http://localhost:8080`

## Как тестировать

1. `./gradlew test`