# PROJECT_STRUCTURE_WITH_LAST_CHANGES.md

## Что это
Файл — актуальная «витрина» структуры проекта + пометки **самых последних изменений**, чтобы вы могли быстро скинуть его в iishke/ИИ для изучения.

> Важно: сейчас помечено то, что точно видно по репозиторию и по изменениям в контроллере авторизации.

---

## Структура проекта

```text
urbash_hair_project/
├─ ADMIN_GUIDE.md
├─ ADMIN_RELATED_FILES.md
├─ ENCRYPTION.md
├─ PROJECT_LISTING.md
├─ PROJECT_STRUCTURE.md
├─ README.md
├─ REVIEWS_RELATED_FILES.md
├─ SENDING_CODES_SYSTEM.md
├─ TODO.md
├─ TODO.txt
├─ pom.xml
└─ src/
   └─ main/
      ├─ java/
      │  └─ by/urbash_hair/
      │     ├─ UrbashHairApplication.java
      │     ├─ config/
      │     │  ├─ BotRegistrar.java
      │     │  ├─ EncryptorInitializer.java
      │     │  ├─ HashUtils.java
      │     │  ├─ JwtAuthenticationFilter.java
      │     │  ├─ JwtService.java
      │     │  ├─ PersonalDataEncryptor.java
      │     │  ├─ SecurityConfig.java
      │     │  └─ WebConfig.java
      │     ├─ controller/
      │     │  ├─ AdminApiController.java
      │     │  ├─ AppointmentController.java
      │     │  ├─ CourseApplicationController.java
      │     │  ├─ GlobalExceptionHandler.java
      │     │  ├─ HomeController.java
      │     │  ├─ JobApplicationController.java
      │     │  ├─ PhoneAuthController.java           <-- LAST CHANGE: OTP + JWT поток (см. ниже)
      │     │  ├─ PostController.java
      │     │  ├─ PublicController.java
      │     │  └─ ReviewController.java
      │     ├─ dto/
      │     │  ├─ AppointmentRequest.java
      │     │  ├─ AuthResponse.java
      │     │  ├─ BookSlotRequest.java
      │     │  ├─ ClientProfileResponse.java
      │     │  ├─ CourseApplicationRequest.java
      │     │  ├─ GenerateSlotsRequest.java
      │     │  ├─ JobApplicationRequest.java
      │     │  ├─ SendCodeRequest.java
      │     │  └─ VerifyCodeRequest.java
      │     ├─ entity/
      │     │  ├─ Applicant.java
      │     │  ├─ Appointment.java
      │     │  ├─ AuditLog.java
      │     │  ├─ Client.java
      │     │  ├─ Course.java
      │     │  ├─ CourseApplication.java
      │     │  ├─ JobApplication.java
      │     │  ├─ Master.java
      │     │  ├─ Post.java
      │     │  ├─ Review.java
      │     │  └─ Service.java
      │     ├─ repository/
      │     │  ├─ ApplicantRepository.java
      │     │  ├─ AppointmentRepository.java
      │     │  ├─ AuditLogRepository.java
      │     │  ├─ ClientRepository.java
      │     │  ├─ CourseApplicationRepository.java
      │     │  ├─ CourseRepository.java
      │     │  ├─ JobApplicationRepository.java
      │     │  ├─ MasterRepository.java
      │     │  ├─ PostRepository.java
      │     │  ├─ ReviewRepository.java
      │     │  └─ ServiceRepository.java
      │     └─ service/
      │        ├─ AppointmentService.java
      │        ├─ AuditLogService.java
      │        ├─ ClientService.java
      │        ├─ CourseApplicationService.java
      │        ├─ CustomUserDetailsService.java
      │        ├─ EmailService.java
      │        ├─ JobApplicationService.java
      │        ├─ PostService.java
      │        ├─ ReviewService.java
      │        ├─ SmsCodeService.java
      │        └─ TelegramPollingBot.java
      └─ resources/
         ├─ application*.yml
         ├─ urbash.env
         ├─ schema.sql
         └─ static/
            ├─ admin.html
            ├─ main.html
            ├─ blog.html
            ├─ work-with-us.html
            ├─ css/...
            └─ js/...
```

---

## LAST CHANGE: PhoneAuthController.java (OTP + Send/Verify + JWT)

**Сопутствующие DTO:**
- `SendCodeRequest` — `phone` *не обязателен*, обязателен `deliveryMethod`
- `VerifyCodeRequest` — `phone` *не обязателен*, обязателен `code` и `deliveryMethod`

(Это влияет на то, как сервер выбирает `<identifier>` для формирования ключа OTP.)


**Файл:** `src/main/java/by/urbash_hair/controller/PhoneAuthController.java`

### Что реализовано / как сейчас работает

- Контроллер: `@RequestMapping("/api/auth")`

#### 1) `POST /api/auth/send-code`
- Генерация 6-значного кода OTP
- Идентификатор пользователя в запросе **зависит от deliveryMethod** и того, что передано в DTO:
  - `phone` (может быть null)
  - `email` (может быть null)
  - `telegramId` (может быть null)
- Формирование ключа хранилища OTP:
  - всегда: `key = <identifier> + ":" + deliveryMethod`
  - где `<identifier>` выбирается так:
    - если `phone` задан → `phone + ":" + deliveryMethod`
    - иначе если `email` задан → `email + ":" + deliveryMethod`
    - иначе → `telegramId + ":" + deliveryMethod`
- Логика поиска/создания клиента:
  - по `emailHash` (если прислан email)
  - или по `phoneHash` (если прислан phone)
  - или по `telegramId` (если прислан telegramId)
  - если клиента не найдено **и передан phone** — создаётся временный `Client` с `phoneHash`
- Отправка кода по `deliveryMethod`:
  - `SMS` — лог/консоль (эмуляция)
  - `EMAIL` — `EmailService.sendVerificationCode(emailTo, code)`
  - `TELEGRAM` — polling-бот:
    - поиск `chatId` по `telegramId`
    - отправка кода в чат

#### 2) `POST /api/auth/verify-code`
- Проверка OTP:
  - ключ OTP такой же логики: `key = <identifier> + ":" + deliveryMethod`
  - где `<identifier>` выбирается:
    - `phone` (если задан) → `phone + ":" + deliveryMethod`
    - иначе `email` (если задан) → `email + ":" + deliveryMethod`
    - иначе `telegramId` → `telegramId + ":" + deliveryMethod`
- `smsCodeService.verifyCode(key, code)`:
  - если код неверный/просроченный → ошибка
- Обязательное согласие (`consentGiven`):
  - если пользователь **создаётся впервые** и `consentGiven == false` → `ConsentRequiredException`
  - если пользователь **существует** и `dataProcessingConsent == false`, то при `consentGiven == false` тоже будет `ConsentRequiredException`
- Если клиент найден:
  - обновляются проф. поля из запроса (`firstName/lastName/middleName/email/telegramId/preferredDelivery`)
- Если клиент не найден:
  - создаётся новый `Client` (заполнение phone/email/telegramId + consent/time)
  - если передан email → выставляется `emailHash`
- OTP удаляется после успешной верификации:
  - `smsCodeService.removeCode(key)`
- Выдача JWT:
  - `jwtService.generateToken(client)`
- Ответ: `AuthResponse` (token + данные пользователя)

---

## TODO/незавершённые пункты (из вашего TODO.txt)
- расписание/календарь:
  - добавить загрузку доступности за месяц
  - подсветка/блокировка занятых слотов
  - админка: таблицы окон/удаление по нажатию

Файл: `TODO.txt`

---

## Куда смотреть для понимания системы защиты данных
- `ENCRYPTION.md`
- `SENDING_CODES_SYSTEM.md`

---

## Как использовать этот файл для изучения
Сначала прочитать:
1) `SENDING_CODES_SYSTEM.md`
2) `ENCRYPTION.md`
3) `PhoneAuthController.java`
4) сопутствующие сервисы:
   - `SmsCodeService`, `EmailService`, `TelegramPollingBot`, `JwtService`

---

## Статус “последних изменений”
Единственное место, которое я пометил как **точно изменённое/актуализированное по содержимому**, — `PhoneAuthController.java`, так как вы показали его в открытых вкладках и я прочитал фактический код.

