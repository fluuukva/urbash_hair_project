# Краткий листинг проекта Urbash Hair

## Основные файлы и директории

**Корень проекта:**
- `pom.xml` - зависимости Maven
- `README.md` - документация
- `PROJECT_STRUCTURE.md` - полная структура
- `.gitignore`

**Java код (`src/main/java/by/urbash_hair/`):**
```
├── UrbashHairApplication.java    # Запуск приложения
├── config/                       # Безопасность, JWT
│   ├── JwtAuthenticationFilter.java
│   ├── JwtService.java
│   ├── SecurityConfig.java
│   └── WebConfig.java
├── controller/                   # API endpoints
│   ├── AdminApiController.java
│   ├── AppointmentController.java
│   ├── CourseApplicationController.java
│   ├── HomeController.java
│   ├── JobApplicationController.java
│   ├── PhoneAuthController.java
│   ├── PostController.java
│   ├── ReviewController.java
│   └── UserController.java
├── dto/                         # DTOs
│   ├── AppointmentRequest.java
│   ├── AuthResponse.java
│   └── ... (7 файлов)
├── entity/                      # БД модели
│   ├── Appointment.java
│   ├── Client.java
│   ├── Master.java
│   ├── Post.java
│   └── ... (10 файлов)
├── repository/                  # Репозитории JPA
│   ├── AppointmentRepository.java
│   └── ... (11 файлов)
└── service/                     # Сервисы
    ├── AppointmentService.java
    ├── SmsCodeService.java
    └── ... (8 файлов)
```

**Ресурсы (`src/main/resources/`):**
```
├── application.yml              # Конфигурация
├── schema.sql                   # Скрипт БД
└── static/                      # Frontend
    ├── admin.html, main.html, blog.html, work-with-us.html
    ├── css/ (17 файлов стилей)
    ├── js/ (8 скриптов: main.js, modal.js и т.д.)
    └── images/ (~30 изображений)
```

**Итого:** ~120 файлов. Backend: Spring Boot + JPA + JWT. Frontend: HTML/CSS/JS.
