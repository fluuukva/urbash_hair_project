# Файлы, связанные с отображением данных в админ-панели

> Этот список поможет при отладке проблем с отображением данных в `/admin.html`.

---

## 1. Frontend (Визуальная часть админки)

| Файл | Описание |
|------|----------|
| `src/main/resources/static/admin.html` | Главная страница админ-панели (таблица, модальные окна, JS-логика загрузки/отображения данных через `fetch` к `/api/admin/**`) |

---

## 2. Backend (REST API админки)

| Файл | Описание |
|------|----------|
| `src/main/java/by/urbash_hair/controller/AdminApiController.java` | Основной контроллер админки. Возвращает список таблиц (`/tables`), данные таблиц (`/data/{table}`), сохранение/удаление записей, загрузку изображений |

---

## 3. Безопасность и доступ (если данные не грузятся — проверьте эти файлы)

| Файл | Описание |
|------|----------|
| `src/main/java/by/urbash_hair/config/SecurityConfig.java` | Настройка Spring Security. Определяет, кто имеет доступ к `/api/admin/**` (требуется роль `ADMIN` или `DATA_OFFICER`) |
| `src/main/java/by/urbash_hair/config/JwtAuthenticationFilter.java` | Фильтр аутентификации. Проверяет JWT-токен из `localStorage` (ключ `token`). Если токен невалиден — запросы к админке отклоняются с 403 |
| `src/main/java/by/urbash_hair/config/JwtService.java` | Сервис генерации и валидации JWT-токенов |
| `src/main/java/by/urbash_hair/service/CustomUserDetailsService.java` | Загрузка данных пользователя по ID из токена. Если пользователь не найден — аутентификация не пройдёт |

---

## 4. Сущности (Entity) — структура отображаемых данных

> Если поля в таблице называются иначе или отсутствуют геттеры/сеттеры — данные могут не сериализоваться в JSON.

| Файл | Описание |
|------|----------|
| `src/main/java/by/urbash_hair/entity/Client.java` | Клиенты (шифрованные поля: ФИО, телефон, email через `PersonalDataEncryptor`) |
| `src/main/java/by/urbash_hair/entity/Appointment.java` | Записи на услуги |
| `src/main/java/by/urbash_hair/entity/JobApplication.java` | Заявки на работу |
| `src/main/java/by/urbash_hair/entity/CourseApplication.java` | Заявки на курсы |
| `src/main/java/by/urbash_hair/entity/Review.java` | Отзывы |
| `src/main/java/by/urbash_hair/entity/Post.java` | Посты блога |
| `src/main/java/by/urbash_hair/entity/Course.java` | Курсы |
| `src/main/java/by/urbash_hair/entity/Service.java` | Услуги салона |
| `src/main/java/by/urbash_hair/entity/Master.java` | Мастера |

---

## 5. Репозитории (доступ к базе данных)

> Если данные пустые — проверьте, что репозитории возвращают записи из БД.

| Файл | Описание |
|------|----------|
| `src/main/java/by/urbash_hair/repository/ClientRepository.java` | Репозиторий клиентов |
| `src/main/java/by/urbash_hair/repository/AppointmentRepository.java` | Репозиторий записей |
| `src/main/java/by/urbash_hair/repository/JobApplicationRepository.java` | Репозиторий заявок на работу |
| `src/main/java/by/urbash_hair/repository/CourseApplicationRepository.java` | Репозиторий заявок на курсы |
| `src/main/java/by/urbash_hair/repository/ReviewRepository.java` | Репозиторий отзывов |
| `src/main/java/by/urbash_hair/repository/PostRepository.java` | Репозиторий постов |
| `src/main/java/by/urbash_hair/repository/CourseRepository.java` | Репозиторий курсов |
| `src/main/java/by/urbash_hair/repository/ServiceRepository.java` | Репозиторий услуг |
| `src/main/java/by/urbash_hair/repository/MasterRepository.java` | Репозиторий мастеров |

---

## 6. DTO (Объекты передачи данных)

| Файл | Описание |
|------|----------|
| `src/main/java/by/urbash_hair/dto/ClientProfileResponse.java` | DTO для отображения данных клиента в админке (метод `fromClient`) |

---

## 7. Шифрование (если поля клиентов отображаются как `null` или кракозябры)

| Файл | Описание |
|------|----------|
| `src/main/java/by/urbash_hair/config/PersonalDataEncryptor.java` | Конвертер шифрования персональных данных (используется в `Client.java`). При ошибке инициализации ключа данные клиентов не расшифруются |
| `src/main/java/by/urbash_hair/config/EncryptorInitializer.java` | Инициализация ключа шифрования при старте приложения |

---

## 8. Сервисы-зависимости

| Файл | Описание |
|------|----------|
| `src/main/java/by/urbash_hair/service/AuditLogService.java` | Логирование действий администратора (используется в `AdminApiController`) |

---

## 9. Конфигурация и БД

| Файл | Описание |
|------|----------|
| `src/main/resources/application.yml` | Конфигурация Spring Boot, подключение к БД, JPA, порт сервера |
| `src/main/resources/schema.sql` | SQL-схема базы данных. Если таблицы не созданы — `findAll()` вернёт пустые списки |

---

## 10. Главный класс приложения

| Файл | Описание |
|------|----------|
| `src/main/java/by/urbash_hair/UrbashHairApplication.java` | Точка входа в Spring Boot приложение |

---

## Краткая схема потока данных

```
admin.html (JS fetch)
    ↓
/api/admin/**  --[SecurityConfig/JwtAuthFilter]-->  AdminApiController
    ↓
Repository.findAll()  ←→  Entity  ←→  БД (schema.sql)
    ↓
JSON (headers + data)  →  renderTable() в admin.html
```

---

## Частые причины поломки отображения

1. **403 Forbidden / "Доступ запрещён"** — проверьте `SecurityConfig.java`, `JwtAuthenticationFilter.java`, наличие `Bearer` токена в `localStorage`.
2. **Пустые таблицы** — проверьте `application.yml` (подключение к БД) и репозитории. Возможно, таблицы пустые или не созданы (`schema.sql`).
3. **Поля клиентов (`fullName`, `phone`, `email`) = null** — проверьте `PersonalDataEncryptor.java` и `EncryptorInitializer.java`. Возможно, ключ шифрования изменился и данные не расшифровываются.
4. **Ошибки 500 при загрузке** — смотрите логи Spring Boot (консоль), возможна ошибка в `AdminApiController.java` при формировании ответа.

