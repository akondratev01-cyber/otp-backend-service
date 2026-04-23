# MANUAL TESTS

Ниже приведены основные сценарии ручной проверки проекта.

## Подготовка
Перед запуском тестов должно быть настроено:
- PostgreSQL
- SQL-инициализация БД
- SMTP
- Telegram bot token / chat id
- SMPP-эмулятор
- backend-приложение

## 1. Логин администратора
Запрос:
`POST /auth/login`

Body:
```json
{
  "login": "demo_auth_user",
  "password": "Password123"
}
```

Ожидаемо:
- возвращается токен администратора

## 2. Логин обычного пользователя
Запрос:
`POST /auth/login`

Body:
```json
{
  "login": "otp_user_1",
  "password": "Password123"
}
```

Ожидаемо:
- возвращается токен пользователя

## 3. Получение информации о текущем пользователе
Запрос:
`GET /users/me`

Ожидаемо:
- возвращаются `id`, `login`, `role`

## 4. Получение списка пользователей администратором
Запрос:
`GET /admin/users`

Ожидаемо:
- возвращается список пользователей без администратора

## 5. Запрет доступа обычного пользователя к admin users API
Запрос:
`GET /admin/users` под USER

Ожидаемо:
- ошибка доступа
- сообщение:
  `Недостаточно прав для доступа к этому ресурсу`

## 6. Получение текущей OTP-конфигурации
Запрос:
`GET /admin/otp-config`

Ожидаемо:
- возвращаются `codeLength`, `ttlSeconds`, `updatedAt`

## 7. Изменение OTP-конфигурации
Запрос:
`PUT /admin/otp-config`

Body:
```json
{
  "codeLength": 6,
  "ttlSeconds": 300
}
```

Ожидаемо:
- конфигурация успешно обновляется

## 8. Генерация OTP через FILE
Запрос:
`POST /otp/generate`

Body:
```json
{
  "operationId": "manual_file_1",
  "channel": "FILE"
}
```

Ожидаемо:
- OTP создается
- код сохраняется в `var/otp/otp-codes.log`

## 9. Валидация OTP
Запрос:
`POST /otp/validate`

Body:
```json
{
  "operationId": "manual_file_1",
  "code": "КОД_ИЗ_ФАЙЛА"
}
```

Ожидаемо:
- `valid = true`

## 10. Повторная валидация использованного OTP
Повторить тот же запрос, что в пункте 9.

Ожидаемо:
- `valid = false`

## 11. Валидация неверного OTP
Запрос:
`POST /otp/validate`

Body:
```json
{
  "operationId": "manual_file_1",
  "code": "000000"
}
```

Ожидаемо:
- `valid = false`

## 12. Генерация OTP через EMAIL
Запрос:
`POST /otp/generate`

Body:
```json
{
  "operationId": "manual_email_1",
  "channel": "EMAIL",
  "email": "your_email@example.com"
}
```

Ожидаемо:
- письмо с OTP приходит на email

## 13. Генерация OTP через TELEGRAM
Запрос:
`POST /otp/generate`

Body:
```json
{
  "operationId": "manual_tg_1",
  "channel": "TELEGRAM"
}
```

Ожидаемо:
- сообщение с OTP приходит в Telegram

## 14. Генерация OTP через SMS
Запрос:
`POST /otp/generate`

Body:
```json
{
  "operationId": "manual_sms_1",
  "channel": "SMS"
}
```

Ожидаемо:
- сообщение уходит в SMPP-эмулятор

## 15. EMAIL без email
Запрос:
`POST /otp/generate`

Body:
```json
{
  "operationId": "manual_email_error_1",
  "channel": "EMAIL"
}
```

Ожидаемо:
- ошибка:
  `Email обязателен для канала EMAIL`

## 16. Неверный канал
Запрос:
`POST /otp/generate`

Body:
```json
{
  "operationId": "manual_bad_channel_1",
  "channel": "PIGEON"
}
```

Ожидаемо:
- ошибка:
  `Неизвестный канал отправки`

## 17. Повторная генерация OTP по той же операции до истечения TTL
### Шаг 1
Запрос:
`POST /otp/generate`

Body:
```json
{
  "operationId": "manual_duplicate_1",
  "channel": "FILE"
}
```

Ожидаемо:
- OTP создан

### Шаг 2
Сразу повторить тот же запрос.

Ожидаемо:
- новый код не создается
- возвращается сообщение вида:
  `Время действия ранее сгенерированного OTP-кода еще не истекло. Повторите попытку через N сек.`

## 18. Logout
Запрос:
`POST /auth/logout`

Ожидаемо:
- токен инвалидируется

## 19. Проверка токена после logout
Повторно вызвать:
`GET /users/me` с тем же токеном

Ожидаемо:
- ошибка:
  `Токен недействителен или истек`
