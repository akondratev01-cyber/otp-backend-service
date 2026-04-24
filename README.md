# OTP Backend Service

Backend-приложение для защиты операций с помощью одноразовых OTP-кодов.

Проект реализован в рамках задания от Promo IT. Основная задача сервиса — создавать OTP-коды, отправлять их пользователю по разным каналам и проверять их при подтверждении операций.

## Технологии
- Java 17
- Maven
- PostgreSQL 17
- JDBC
- `com.sun.net.httpserver`
- Logback
- Jakarta Mail / Angus Mail
- JSMpp
- Telegram Bot API

## Возможности проекта
- регистрация нового пользователя
- вход пользователя в систему
- выход пользователя из системы
- токенная аутентификация и авторизация
- роли:
    - `ADMIN`
    - `USER`
- генерация OTP-кодов
- валидация OTP-кодов
- настройка длины и времени жизни OTP через admin API
- автоматический перевод истекших кодов в статус `EXPIRED`
- доставка OTP через:
    - FILE
    - EMAIL
    - TELEGRAM
    - SMS (через SMPP-эмулятор)

## Архитектура
Приложение построено по слоям:
- слой контроллеров / API
- слой сервисов
- слой DAO / репозиториев

### Основные модули
- `auth` — аутентификация, токены
- `user` — пользовательское API
- `admin` — административное API
- `otp` — доменная логика OTP
- `notification` — каналы отправки кодов
- `config` — конфигурация приложения
- `common` — общие HTTP / JSON / error utilities
- `demo` — вспомогательные demo main-классы

## Структура базы данных
В проекте используется PostgreSQL 17 и JDBC. Реализованы основные таблицы:
- пользователи
- конфигурация OTP
- OTP-коды

Статусы OTP:
- `ACTIVE`
- `EXPIRED`
- `USED`

## Особенности бизнес-логики

### Один активный OTP на одну операцию
Для одной пользовательской операции не создается несколько параллельных активных OTP-кодов.

Если для `userId + operationId` уже существует `ACTIVE` код и его TTL еще не истек:
- новый код не создается;
- повторная отправка в канал не выполняется;
- API возвращает информативное сообщение с оставшимся временем ожидания.

Это делает поведение сервиса предсказуемым и исключает неоднозначность при подтверждении одной и той же операции.

## Как запустить проект

### 1. Подготовить PostgreSQL
Нужен локальный PostgreSQL 17.

### 2. Создать БД и применить SQL-инициализацию
Выполнить SQL-скрипт:
- `sql/01_init.sql`

### 3. Настроить конфигурацию приложения
Необходимо указать параметры:
- подключения к БД
- SMTP
- Telegram bot token / chat id
- SMPP host / port / credentials

При необходимости можно использовать локальный файл переопределения настроек, например `application-local.properties`.

### 4. Настроить EMAIL
Для email используется SMTP.

Нужно указать:
- SMTP host
- SMTP port
- username
- password
- from email

### 5. Настроить TELEGRAM
Для Telegram необходимо:
- создать бота через `@BotFather`
- получить bot token
- начать диалог с ботом
- получить `chatId` через Telegram Bot API `getUpdates`

### 6. Настроить SMS / SMPP
Для SMS используется SMPP-эмулятор.

Нужно:
- запустить SMPP-эмулятор
- указать host / port / system_id / password / system_type / source_addr

### 7. Запустить приложение
```bash
mvn clean package
mvn exec:java
```

## Поддерживаемые API endpoints

### Auth
- `POST /auth/register` — регистрация нового пользователя
- `POST /auth/login` — логин пользователя
- `POST /auth/logout` — выход пользователя

### User API
- `GET /users/me` — информация о текущем пользователе

### Admin API
- `GET /admin/users` — список пользователей кроме администратора
- `DELETE /admin/users/{id}` — удаление пользователя
- `GET /admin/otp-config` — чтение текущей OTP-конфигурации
- `PUT /admin/otp-config` — изменение длины и TTL OTP-кода

### OTP API
- `POST /otp/generate` — генерация OTP-кода
- `POST /otp/validate` — валидация OTP-кода

## Примеры сценариев

### Регистрация пользователя
`POST /auth/register`

Пример body:
```json
{
  "login": "otp_user_1",
  "password": "Password123"
}
```

### Логин пользователя
`POST /auth/login`

Пример body:
```json
{
  "login": "otp_user_1",
  "password": "Password123"
}
```

### Генерация OTP
`POST /otp/generate`

Пример body:
```json
{
  "operationId": "payment_001",
  "channel": "FILE"
}
```

Пример для EMAIL:
```json
{
  "operationId": "payment_002",
  "channel": "EMAIL",
  "email": "user@example.com"
}
```

### Валидация OTP
`POST /otp/validate`

Пример body:
```json
{
  "operationId": "payment_001",
  "code": "123456"
}
```

### Изменение OTP-конфигурации администратором
`PUT /admin/otp-config`

Пример body:
```json
{
  "codeLength": 6,
  "ttlSeconds": 300
}
```

## Как тестировать проект

### Основные сценарии ручной проверки
В проекте были вручную проверены следующие сценарии:
- login / logout
- `/users/me`
- `GET /admin/users`
- `GET /admin/otp-config`
- `PUT /admin/otp-config`
- `POST /otp/generate`
- `POST /otp/validate`
- повторная валидация использованного кода
- неверный OTP-код
- запрет доступа USER к admin API
- повторная генерация OTP по той же операции до истечения TTL
- FILE / EMAIL / TELEGRAM / SMS

### Проверка каналов доставки
- `FILE` — код сохраняется в файл `var/otp/otp-codes.log`
- `EMAIL` — код приходит на email
- `TELEGRAM` — код приходит через Telegram-бота
- `SMS` — код уходит через SMPP-эмулятор

### Файлы для проверки
В проекте подготовлены дополнительные файлы для проверки:
- `MANUAL_TESTS.md` — ручные сценарии проверки
- `demo.http` — набор HTTP-запросов для IntelliJ IDEA / HTTP Client

## Логирование
В приложении настроено логирование через Logback.

## Что проверить перед запуском
Перед проверкой проекта нужно убедиться, что:
- PostgreSQL запущен
- SQL-инициализация выполнена
- конфигурация БД корректна
- SMTP настроен
- Telegram bot token и chat id указаны
- SMPP-эмулятор запущен
