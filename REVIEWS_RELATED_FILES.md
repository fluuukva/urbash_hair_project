# Файлы, связанные с отзывами (reviews / комментариями)

> Этот список поможет при отладке проблем с отображением, созданием или удалением отзывов.

---

## 1. Frontend (Визуальная часть — отображение и отправка отзывов)

| Файл | Описание |
|------|----------|
| `src/main/resources/static/main.html` | Главная страница. Содержит секцию `#reviews` с каруселью отзывов, поиском и формой для написания нового отзыва |
| `src/main/resources/static/css/reviews.css` | Стили карусели отзывов, карточек отзывов, звёздочек рейтинга, формы отправки и адаптивности |
| `src/main/resources/static/js/reviews.js` | Логика работы с отзывами: `fetchReviews()` (загрузка с API), `renderReviewsToTrack()` (рендер карусели), отправка нового отзыва (`POST /api/reviews`), поиск по отзывам, переключение слайдов |

---

## 2. Backend API (Публичное API для отзывов)

| Файл | Описание |
|------|----------|
| `src/main/java/by/urbash_hair/controller/ReviewController.java` | REST-контроллер `/api/reviews`. Обрабатывает `GET /api/reviews` (получить все отзывы) и `POST /api/reviews` (создать отзыв). При создании привязывает клиента через `ClientRepository` |
| `src/main/java/by/urbash_hair/service/ReviewService.java` | Сервисный слой: `getAll()` и `create()` — делегирует вызовы в `ReviewRepository` |
| `src/main/java/by/urbash_hair/repository/ReviewRepository.java` | JPA-репозиторий для сущности `Review`. Расширяет `JpaRepository<Review, Long>` |
| `src/main/java/by/urbash_hair/entity/Review.java` | Сущность "Отзыв" (`@Table(name = "отзыв")`). Поля: `id`, `client` (ManyToOne), `rating`, `comment`, `date` |

---

## 3. Админ-панель (Управление отзывами)

| Файл | Описание |
|------|----------|
| `src/main/java/by/urbash_hair/controller/AdminApiController.java` | Админ API: возвращает отзывы через `/api/admin/data/reviews`, удаляет отзывы через `/api/admin/delete/reviews`, логирует действия в `AuditLogService` |
| `src/main/resources/static/admin.html` | Универсальная админ-панель. Отображает таблицу "Отзывы" с колонками: ID, Клиент, Рейтинг, Комментарий, Дата. Поддерживает удаление выбранных записей |

---

## 4. Связанные сущности (Клиент — автор отзыва)

| Файл | Описание |
|------|----------|
| `src/main/java/by/urbash_hair/entity/Client.java` | Клиент. Отзыв связан с клиентом через `@ManyToOne`. Используется для отображения имени автора отзыва |
| `src/main/java/by/urbash_hair/repository/ClientRepository.java` | Репозиторий клиентов. Используется в `ReviewController` при создании отзыва для поиска и привязки клиента по `id` |

---

## 5. Безопасность (Доступ к API отзывов)

| Файл | Описание |
|------|----------|
| `src/main/java/by/urbash_hair/config/SecurityConfig.java` | Spring Security: разрешает публичный доступ (`permitAll`) к `/api/reviews/**` — отзывы могут просматривать и оставлять все пользователи без авторизации |
| `src/main/java/by/urbash_hair/config/JwtAuthenticationFilter.java` | JWT-фильтр: путь `/api/reviews` указан в `isPublicPath()`, поэтому запросы к отзывам проходят без проверки токена |

---

## 6. База данных

| Файл | Описание |
|------|----------|
| `src/main/resources/schema.sql` | SQL-схема. Содержит `CREATE TABLE IF NOT EXISTS \`отзыв\`` с полями: `id_Отзыва`, `id_Клиента`, `Оценка`, `Комментарий`, `Дата`. Внешний ключ на таблицу `клиент` |

---

## 7. Дополнительные зависимости

| Файл | Описание |
|------|----------|
| `src/main/java/by/urbash_hair/service/AuditLogService.java` | Логирование действий администратора. Используется при удалении отзывов через админ-панель |
| `src/main/resources/application.yml` | Конфигурация приложения и подключения к БД. Если БД недоступна — отзывы не загрузятся |

---

## Схема потока данных (отзывы)

```
[Пользователь] → main.html (reviews.js)
                      ↓
               GET /api/reviews ←→ ReviewController → ReviewService → ReviewRepository → Review Entity → БД (отзыв)
                      ↓
               renderReviewsToTrack() → карусель отзывов

[Пользователь пишет отзыв] → POST /api/reviews (с client.id, rating, comment, date)
                                    ↓
                            ReviewController → ClientRepository.findById() → привязка клиента
                                    ↓
                            ReviewService.create() → ReviewRepository.save() → БД

[Администратор] → admin.html → /api/admin/data/reviews → AdminApiController → ReviewRepository.findAll()
                      ↓
               DELETE /api/admin/delete/reviews → AdminApiController → AuditLogService.log()
```

---

## Частые причины поломки отзывов

1. **Отзывы не загружаются на главной** — проверьте `ReviewController.java`, `ReviewService.java`, доступность БД (`application.yml`) и консоль браузера на ошибки CORS/network.
2. **Не отправляется новый отзыв** — проверьте `reviews.js` (функция отправки), `ReviewController.java` (метод `create`), и что `rating > 0` и `comment` не пустые.
3. **Имя клиента отображается как "Аноним"** — проверьте связь `Review.client` (ManyToOne), загрузку клиента в `ReviewController`, и что в `reviews.js` корректно читается `review.client.firstName` / `review.client.lastName`.
4. **В админке пустая таблица отзывов** — проверьте `AdminApiController.java` (case `reviews` в методе `getTableData`), `ReviewRepository.java` и наличие записей в БД.
5. **Ошибка при удалении отзыва в админке** — проверьте права доступа (`SecurityConfig`), внешние ключи в БД и `AuditLogService`.

