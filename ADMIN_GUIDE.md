# Инструкция по запуску админ-панели Urbash Hair

## 1. Предварительные требования

Перед запуском убедитесь, что установлены:

- **Java Development Kit (JDK) 21**
- **Apache Maven 3.8+**
- **MySQL Server 8.0+**

Проверьте версии:
```bash
java -version
mvn -version
mysql --version
```

## 2. Подготовка базы данных

1. Запустите MySQL Server.
2. Создайте базу данных (название должно совпадать с `application.yml`):
   ```bash
   mysql -u root -p
   ```
   ```sql
   CREATE DATABASE beauty_salon_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Убедитесь, что в `src/main/resources/application.yml` указаны корректные данные для подключения:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/beauty_salon_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
       username: root
       password: 1234
   ```

При первом запуске таблицы создадутся автоматически из `schema.sql`.

## 3. Запуск приложения

### Вариант А: Через Maven (рекомендуется для разработки)

В корневой папке проекта выполните:
```bash
mvn spring-boot:run
```

### Вариант Б: Сборка JAR и запуск

```bash
mvn clean package -DskipTests
java -jar target/urbash_hair_project-1.0.0.jar
```

### Вариант В: Из IDE

Откройте проект в IntelliJ IDEA / Eclipse и запустите класс `UrbashHairApplication.java` как Spring Boot Application.

## 4. Адрес админ-панели

После успешного запуска приложение доступно по адресу:
```
http://localhost:8080
```

Админ-панель находится по прямому URL:
```
http://localhost:8080/admin.html
```

## 5. Как войти в админ-панель (аутентификация)

Приложение использует **JWT-аутентификацию через SMS-код**.

### 5.1 Как определяется администратор

Роль `ADMIN` назначается автоматически по номеру телефона, указанному в `application.yml`:
```yaml
admin:
  phone: +375333737505
```

Только клиент с этим номером телефона получит роль администратора. Все остальные пользователи получают роль `CLIENT`.

### 5.2 Получение JWT-токена (шаги)

Так как в текущей реализации SMS-сервис эмулирован, код возвращается прямо в ответе API.

#### Шаг 1: Запросить SMS-код

Отправьте POST-запрос на `/api/auth/send-code`:

**curl:**
```bash
curl -X POST http://localhost:8080/api/auth/send-code \
  -H "Content-Type: application/json" \
  -d '{"phone": "+375333737505"}'
```

**Или через Postman / любой HTTP-клиент:**
- Метод: `POST`
- URL: `http://localhost:8080/api/auth/send-code`
- Body (JSON):
  ```json
  {
    "phone": "+375333737505"
  }
  ```

**Ответ:** шестизначный код (например, `123456`).

#### Шаг 2: Верифицировать код и получить токен

Отправьте POST-запрос на `/api/auth/verify-code`:

**curl:**
```bash
curl -X POST http://localhost:8080/api/auth/verify-code \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+375333737505",
    "code": "123456",
    "consentGiven": true,
    "firstName": "Админ",
    "lastName": "Админов"
  }'
```

**Или через Postman:**
- Метод: `POST`
- URL: `http://localhost:8080/api/auth/verify-code`
- Body (JSON):
  ```json
  {
    "phone": "+375333737505",
    "code": "123456",
    "consentGiven": true,
    "firstName": "Админ",
    "lastName": "Админов"
  }
  ```

**В ответе вы получите JSON с полем `token`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "id": 1,
  "firstName": "Админ",
  "lastName": "Админов",
  "phone": "+375333737505"
}
```

### 5.3 Сохранение токена для доступа к админке

Админ-панель (`admin.html`) читает токен из `localStorage` браузера по ключу `token`.

**Способ 1: Через консоль браузера**
1. Откройте `http://localhost:8080/admin.html`
2. Откройте DevTools (F12) → вкладка Console
3. Выполните:
   ```javascript
   localStorage.setItem('token', 'eyJhbGciOiJIUzI1NiIs...');
   ```
   (вставьте свой токен из шага 2)
4. Перезагрузите страницу (F5)

**Способ 2: Встроенный вход (если реализован на main.html)**
На главной странице (`main.html`) обычно реализован модальный вход по SMS. Пройдите авторизацию там с админским номером, затем перейдите на `/admin.html`.

## 6. Проверка работы админ-панели

После сохранения токена и перезагрузки `admin.html`:
- В левом меню должны загрузиться таблицы (запрос `/api/admin/tables`).
- Если вместо таблиц отображается ошибка "Доступ запрещён" — значит, токен отсутствует, просрочен или номер телефона не совпадает с `admin.phone`.

## 7. Возможные проблемы

### Порт 8080 занят
```
Port 8080 is already in use
```
**Решение:** измените порт в `application.yml`:
```yaml
server:
  port: 8081
```

### Ошибка подключения к MySQL
```
Communications link failure / Access denied
```
**Решение:**
- Проверьте, что MySQL запущен.
- Проверьте логин/пароль в `application.yml`.
- Убедитесь, что база `beauty_salon_db` создана.

### Ошибка 403 (Forbidden) в админ-панели
**Причины:**
- В `localStorage` нет токена.
- Токен просрочен (срок жизни — 24 часа).
- Вы вошли с номером, отличным от `admin.phone`.

**Решение:** повторите шаги 5.1–5.3 с административным номером.

### Несоответствие endpoint'ов
Файл `admin.html` обращается к универсальным endpoint'ам (`/api/admin/tables`, `/api/admin/data/...`, `/api/admin/save/...`), тогда как `AdminApiController` содержит только специфичные endpoint'ы (`/api/admin/clients`, `/api/admin/appointments` и т.д.).

Если при загрузке админ-панели вы видите ошибку `404` на `/api/admin/tables`, это означает, что универсальный CRUD-контроллер для админки ещё не реализован. В этом случае админ-панель не сможет отобразить таблицы до реализации соответствующих endpoint'ов на бэкенде.

## 8. Быстрая шпаргалка

```bash
# 1. Запустить MySQL и создать БД
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS beauty_salon_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. Запустить приложение
mvn spring-boot:run

# 3. Получить SMS-код
curl -X POST http://localhost:8080/api/auth/send-code \
  -H "Content-Type: application/json" \
  -d '{"phone": "+375333737505"}'

# 4. Верифицировать код и получить токен
curl -X POST http://localhost:8080/api/auth/verify-code \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+375333737505",
    "code": "КОД_ИЗ_ШАГА_3",
    "consentGiven": true,
    "firstName": "Админ",
    "lastName": "Админов"
  }'

# 5. Открыть в браузере http://localhost:8080/admin.html
#    и вставить токен в localStorage через консоль DevTools
```

