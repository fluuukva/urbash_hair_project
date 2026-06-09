# Система отправки кодов подтверждения (Telegram и Email)

В проекте URBASH.hair используется **однократный код (OTP)** для подтверждения пользователя. Код действует **5 минут** и отправляется выбранным способом: **Telegram** или **Email**.

---

## 1) Точка входа: API авторизации

### Отправить код
**POST** `/api/auth/send-code`

Клиент передаёт:
- `phone` — телефон (используется как идентификатор/ключ для хранения кода)
- `deliveryMethod` — канал доставки: `SMS`, `EMAIL`, `TELEGRAM` (по задаче интересуют `EMAIL` и `TELEGRAM`)
- для `EMAIL`: `email`
- для `TELEGRAM`: `telegramId` (username, ожидается строка вида `@username` или имя из Telegram)

Что делает сервер:
1. Генерирует 6-значный код.
2. Сохраняет код во временном хранилище с ключом:
   - `key = phone + ":" + deliveryMethod`
3. Отправляет код через выбранный канал.

Логика реализована в `PhoneAuthController`.

### Проверить код
**POST** `/api/auth/verify-code`

Клиент передаёт:
- `phone`
- `deliveryMethod`
- `code`
- (и другие данные профиля при необходимости)

Что делает сервер:
1. Строит тот же ключ: `key = phone + ":" + deliveryMethod`
2. Проверяет код в хранилище:
   - код должен существовать
   - код не должен быть просрочен (истечение через 5 минут)
3. При успехе удаляет код из хранилища.
4. Создаёт/обновляет клиента в БД и выдаёт JWT.

---

## 2) Как код отправляется в Email

Отправка Email реализована через `EmailService`.

**Процесс**:
1. В `PhoneAuthController` при `deliveryMethod = "EMAIL"` берётся `email` из запроса.
2. Вызывается `emailService.sendVerificationCode(emailTo, code)`.
3. `EmailService` формирует письмо:
   - тема: `URBASH.hair: Код подтверждения`
   - текст: `Ваш код подтверждения: <code>` + пометка “действителен 5 минут”.
4. `JavaMailSender` отправляет письмо через настроенный SMTP.

---

## 3) Как код отправляется в Telegram

Telegram-часть построена на **polling-боте** (долгий опрос Telegram API).

### 3.1 Регистрация username → chatId
Реализовано в `TelegramPollingBot`.

1. Бот обрабатывает входящие сообщения в `onUpdateReceived`.
2. При команде **`/start`** бот получает `username` пользователя.
3. Бот сохраняет соответствие:
   - ключ: `"@" + username`
   - значение: `chatId`

Это нужно, чтобы затем отправлять сообщения конкретному пользователю.

### 3.2 Отправка кода
**Процесс отправки** (в `PhoneAuthController` при `deliveryMethod = "TELEGRAM"`):
1. Из запроса берётся `telegramId` (username).
2. Выполняется поиск `chatId`:
   - `Long chatId = telegramPollingBot.getChatIdByUsername(input)`
3. Если `chatId` не найден — сервер возвращает ошибку:
   - пользователь не присылал `/start`
   - username не публичный или не установлен
4. Если `chatId` найден — вызывается:
   - `telegramPollingBot.sendVerificationCode(chatId, code)`
5. `TelegramPollingBot` отправляет сообщение через `SendMessage`:
   - `Ваш код подтверждения для сайта URBASH.hair: <code>`
   - пометка “Код действителен 5 минут”.

---

## 4) Где хранится код (важно)

Коды **не хранятся в БД** — они лежат в памяти сервиса.

За это отвечает `SmsCodeService` (он же используется как общий сервис для кодов):
- хранилище: `ConcurrentHashMap<String, CodeEntry>`
- запись: `saveCode(key, code)`
- проверка: `verifyCode(key, code)`
- истечение: через `CODE_EXPIRATION_SECONDS = 300` секунд

Следствие:
- при перезапуске сервера коды сбрасываются
- система подходит для OTP-подтверждения, но требует внимания к масштабированию (несколько инстансов = разные Map)

---

## 5) Небольшая схема потока

**Отправка (send-code)**
1) `/api/auth/send-code` → генерировать код
2) сохранить: `phone:deliveryMethod`
3) отправить:
   - Email → `EmailService`
   - Telegram → `TelegramPollingBot` (username → chatId)

**Подтверждение (verify-code)**
1) `/api/auth/verify-code` → найти код по ключу
2) проверить срок (5 минут)
3) удалить код
4) выдать JWT

---

## 6) Места в коде
- `src/main/java/by/urbash_hair/controller/PhoneAuthController.java`
- `src/main/java/by/urbash_hair/service/EmailService.java`
- `src/main/java/by/urbash_hair/service/TelegramPollingBot.java`
- `src/main/java/by/urbash_hair/service/SmsCodeService.java` (временное хранилище + проверка/истечение)

