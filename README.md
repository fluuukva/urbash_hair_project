# Urbash Hair Project

Салон красоты «Urbash Hair» — веб-приложение для управления салоном красоты с возможностью записи на услуги, просмотра работ мастеров, отзывов и подачи заявок на работу.

## Технологический стек

- **Backend:** Spring Boot 3.2.11, Java 21
- **Database:** MySQL
- **Authentication:** JWT
- **Image Storage:** ImageKit
- **Build Tool:** Maven
- **Frontend:** HTML, CSS, JavaScript

## Требования для развертывания

Перед запуском проекта убедитесь, что на вашем компьютере установлены:

1. **Java Development Kit (JDK) 21** — [Скачать](https://www.oracle.com/java/technologies/downloads/#java21)
2. **Maven 3.8+** — [Скачать](https://maven.apache.org/download.cgi)
3. **MySQL Server 8.0+** — [Скачать](https://dev.mysql.com/downloads/mysql/)
4. **Git** (опционально) — [Скачать](https://git-scm.com/downloads)

## Инструкция по развертыванию

### Шаг 1: Скачивание проекта из GitHub

1. Откройте браузер и перейдите на [репозиторий проекта](https://github.com/your-username/urbash_hair_project)
2. Нажмите на зелёную кнопку **"Code"**
3. Нажмите **"Download ZIP"**
4. Распакуйте скачанный архив в удобную для вас папку

Альтернативно, используя Git:
```bash
git clone https://github.com/your-username/urbash_hair_project.git
cd urbash_hair_project
```

### Шаг 2: Настройка MySQL базы данных

1. Запустите MySQL Server
2. Войдите в MySQL через терминал или MySQL Workbench:
   ```bash
   mysql -u root -p
   ```
3. Создайте базу данных:
   ```sql
   CREATE DATABASE beauty_salon_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
4. Настройте пользователя (или используйте root):
   ```sql
   CREATE USER 'root'@'localhost' IDENTIFIED BY '1234';
   GRANT ALL PRIVILEGES ON beauty_salon_db.* TO 'root'@'localhost';
   FLUSH PRIVILEGES;
   ```

### Шаг 3: Настройка приложения

1. Откройте файл `src/main/resources/application.yml`
2. При необходимости измените настройки подключения к базе данных:
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/beauty_salon_db
       username: root
       password: 1234
   ```
3. Настройте порт сервера (необязательно):
   ```yaml
   server:
     port: 8080  # Измените на нужный порт
   ```

### Шаг 4: Сборка и запуск проекта

#### Вариант 1: С использованием Maven

1. Откройте терминал в корневой папке проекта
2. Соберите проект:
   ```bash
   mvn clean package -DskipTests
   ```
3. Запустите приложение:
   ```bash
   java -jar target/urbash_hair_project-1.0.0.jar
   ```

#### Вариант 2: С использованием Maven в режиме разработки

```bash
mvn spring-boot:run
```

#### Вариант 3: Запуск из IDE

1. Откройте проект в вашей IDE (IntelliJ IDEA, Eclipse и т.д.)
2. Найдите класс `UrbashHairApplication.java`
3. Запустите его как Spring Boot приложение

### Шаг 5: Проверка работы приложения

После успешного запуска:

1. Откройте браузер и перейдите по адресу: `http://localhost:8080`
2. Вы должны увидеть главную страницу салона красоты

## Структура проекта

```
urbash_hair_project/
├── src/main/
│   ├── java/by/urbash_hair/
│   │   ├── config/          # Конфигурация безопасности и JWT
│   │   ├── controller/     # REST контроллеры
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # Сущности базы данных
│   │   ├── repository/     # Репозитории JPA
│   │   └── service/        # Бизнес-логика
│   └── resources/
│       ├── application.yml # Настройки приложения
│       ├── schema.sql      # SQL-схема базы данных
│       └── static/         # Статические файлы (HTML, CSS, JS)
├── pom.xml                 # Конфигурация Maven
└── README.md               # Этот файл
```

## Доступ к админ-панели

Админ-панель доступна по адресу: `http://localhost:8080/admin.html`

Для входа используйте телефон, указанный в конфигурации:
- Телефон администратора: +375333737505

## Возможные проблемы и решения

### Ошибка подключения к базе данных

**Проблема:** `Communications link failure` или `Access denied`

**Решение:**
- Убедитесь, что MySQL запущен
- Проверьте имя пользователя и пароль в `application.yml`
- Проверьте, что база данных `beauty_salon_db` создана

### Ошибка порта

**Проблема:** `Port 8080 is already in use`

**Решение:**
- Измените порт в `application.yml` на свободный
- Или остановите приложение, занимающее порт 8080

### Ошибка Java версии

**Проблема:** `Unsupported class file major version`

**Решение:**
- Убедитесь, что используется Java 21
- Проверьте версию: `java -version`
- Настройте JAVA_HOME на JDK 21

## Настройка ImageKit (опционально)

Для работы с изображениями необходимо настроить ImageKit:

1. Зарегистрируйтесь на [ImageKit.io](https://imagekit.io/)
2. Создайте новый проект
3. Скопируйте Public Key, Private Key и URL Endpoint
4. Обновите настройки в `application.yml`:
   ```yaml
   imagekit:
     public-key: ваш_public_key
     private-key: ваш_private_key
     url-endpoint: ваш_url_endpoint
   ```

## Сборка production-версии

```bash
mvn clean package -DskipTests
```

Скомпилированный JAR-файл будет находится в папке `target/`.

## Лицензия

Проект распространяется по лицензии MIT.

