# PROJECT_STRUCTURE.md

## Визуальная структура проекта

```
urbash_hair_project/
├─ ADMIN_GUIDE.md
├─ ADMIN_RELATED_FILES.md
├─ ENCRYPTION.md
├─ PROJECT_LISTING.md
├─ PROJECT_STRUCTURE.md      <-- (схема/обновление структуры)
├─ README.md
├─ REVIEWS_RELATED_FILES.md
├─ SENDING_CODES_SYSTEM.md   <-- система OTP для Telegram/Email
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
      │     │  ├─ PhoneAuthController.java          <-- send/verify code
      │     │  ├─ PostController.java
      │     │  ├─ PublicController.java
      │     │  └─ ReviewController.java
      │     └─ dto/
      │        ├─ AppointmentRequest.java
      │        ├─ AuthResponse.java
      │        ├─ BookSlotRequest.java
      │        ├─ ClientProfileResponse.java
      │        ├─ CourseApplicationRequest.java
      │        ├─ GenerateSlotsRequest.java
      │        ├─ JobApplicationRequest.java
      │        ├─ SendCodeRequest.java             <-- request DTO
      │        └─ VerifyCodeRequest.java          <-- request DTO
      │     ├─ entity/
      │     │  ├─ Client.java
      │     │  ├─ AuditLog.java
      │     │  ├─ Appointment.java
      │     │  └─ ...
      │     ├─ repository/
      │     │  └─ ... (JpaRepository)
      │     └─ service/
      │        ├─ SmsCodeService.java             <-- хранение OTP в памяти
      │        ├─ EmailService.java               <-- SMTP отправка кода
      │        ├─ TelegramPollingBot.java       <-- polling-бот: username->chatId
      │        └─ ... (Appointment/Review/etc.)
      └─ resources/
         ├─ application.yml
         ├─ schema.sql
         └─ static/
            ├─ main.html
            ├─ admin.html
            ├─ blog.html
            ├─ work-with-us.html
            ├─ css/
            └─ js/
               ├─ config.js
               └─ main.js
```

## Ключевые сервисы OTP (Telegram / Email)

Документация: `SENDING_CODES_SYSTEM.md`

- `PhoneAuthController`
  - `POST /api/auth/send-code`
  - `POST /api/auth/verify-code`
- `SmsCodeService`
  - временное хранение кодов в `ConcurrentHashMap`
  - истечение: 300 секунд
- `EmailService`
  - отправка письма через `JavaMailSender`
- `TelegramPollingBot`
  - принимает `/start` и сохраняет `username -> chatId`
  - отправляет код через `SendMessage`

## Примечание про статические ресурсы

HTML/JS/CSS лежат в:
- `src/main/resources/static/`

Spring Boot раздаёт их как статический контент.

