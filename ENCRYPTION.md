# Описание системы шифрования и защиты персональных данных

## Общая архитектура

В проекте используется **двухуровневая защита персональных данных**:

1. **Шифрование конфиденциальных полей** (AES-256-GCM) — данные в БД хранятся в зашифрованном виде, автоматически шифруются при сохранении и расшифровываются при чтении.
2. **Детерминированный хеш (SHA-256 с солью)** — используется как «слепой индекс» (blind index) для поиска клиентов по телефону/email без расшифровки.
3. **JWT-аутентификация** — токены подписываются секретным ключом (HMAC-SHA256).

---

## 1. Файлы конфигурации секретов

### `src/main/resources/application.yml`

Содержит **только ссылки на переменные окружения**, сами секреты в репозитории не хранятся.

```yaml
jasypt:
  encryptor:
    password: ${JASYPT_ENCRYPTOR_PASSWORD}   # пароль для AES-шифрования ПДн

app:
  security:
    phone-hash-salt: ${PHONE_HASH_SALT}      # соль для SHA-256 хеша

application:
  security:
    jwt:
      secret-key: ${JWT_SECRET}              # ключ подписи JWT (мин. 32 байта)
```

**Обязательные переменные окружения на сервере:**
```bash
export DB_PASSWORD=<пароль_к_бд>
export JASYPT_ENCRYPTOR_PASSWORD=<пароль_для_jasypt>
export PHONE_HASH_SALT=<соль_для_хеша>
export JWT_SECRET=<секретный_ключ_jwt_минимум_32_байта>
```

---

## 2. Компоненты шифрования

### 2.1 `src/main/java/by/urbash_hair/config/PersonalDataEncryptor.java`

**Назначение:** JPA `AttributeConverter`, автоматически шифрует/расшифровывает строковые поля сущностей при операциях с БД.

**Алгоритм:** `AES/GCM/NoPadding`
- **AES-256** — ключ 32 байта (256 бит).
- **GCM** — режим аутентифицированного шифрования (обеспечивает конфиденциальность и целостность).
- **IV (nonce)** — 12 байт, генерируется случайно (`SecureRandom`) для каждой операции шифрования.
- **Authentication tag** — 128 бит.

**Формат хранения в БД:**
```
Base64( IV (12 байт) || encryptedData + tag )
```

**Ключ:** формируется из пароля `jasypt.encryptor.password` — берутся байты пароля в UTF-8 и копируются в массив фиксированной длины 32 байта (если пароль короче, остаток заполняется нулями).

**Код:**
```java
@Component
@Converter
public class PersonalDataEncryptor implements AttributeConverter<String, String> {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;
    private final SecretKey secretKey;

    public PersonalDataEncryptor(@Value("${jasypt.encryptor.password}") String password) {
        byte[] keyBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] fixedKey = new byte[32];
        System.arraycopy(keyBytes, 0, fixedKey, 0, Math.min(keyBytes.length, fixedKey.length));
        this.secretKey = new SecretKeySpec(fixedKey, "AES");
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        // Генерация IV → шифрование → склейка IV + ciphertext → Base64
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        // Base64 decode → разделение IV и ciphertext → расшифровка
    }
}
```

**Важно:** `@Component` + `@Converter` позволяет Spring инжектировать `@Value` в конвертер.

---

### 2.2 `src/main/java/by/urbash_hair/config/HashUtils.java`

**Назначение:** Создание детерминированных хешей телефона и email для использования в качестве **слепых индексов** (blind indexes). Позволяет искать клиентов по телефону/email, не расшифровывая зашифрованные данные.

**Алгоритм:** SHA-256 с солью.
- К телефону/email добавляется соль (`app.security.phone-hash-salt`).
- Результат — hex-строка длиной 64 символа.

**Код:**
```java
@Component
public class HashUtils {
    private final String salt;

    public HashUtils(@Value("${app.security.phone-hash-salt}") String salt) {
        this.salt = salt;
    }

    public String hashPhone(String phone) {
        return sha256(phone + salt);
    }

    public String hashEmail(String email) {
        return sha256(email + salt);
    }

    private String sha256(String input) {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        // преобразование в hex-строку
    }
}
```

**Почему детерминированный:** Одинаковый телефон всегда даёт одинаковый хеш → можно делать `SELECT ... WHERE phone_hash = ?`.

---

### 2.3 `src/main/java/by/urbash_hair/config/JwtService.java`

**Назначение:** Генерация и валидация JWT-токенов для аутентификации.

**Алгоритм подписи:** HMAC-SHA256 (`Jwts.SIG.HS256`).

**Структура токена:**
- `subject` — ID клиента (`client.getId()`).
- `role` — роль клиента (`CLIENT` или `ADMIN`), определяется по совпадению телефона с `admin.phone`.
- `issuedAt` / `expiration` — время выдачи и истечения (access: 24ч, refresh: 7д).

**Ключ:** `application.security.jwt.secret-key` — base64-декодируется перед использованием.

**Ключевые методы:**
```java
public String generateToken(Client client)   // access-token с ролью
public String generateRefreshToken(UserDetails userDetails)
public boolean isTokenValid(String token, UserDetails userDetails)
public String extractRole(String token)
public Long extractUserId(String token)
```

---

## 3. Сущности с защищёнными полями

### 3.1 `src/main/java/by/urbash_hair/entity/Client.java`

Единственная сущность, поля которой шифруются перед сохранением в БД.

**Зашифрованные поля** (`@Convert(converter = PersonalDataEncryptor.class)`):
| Поле | Колонка БД |
|------|-----------|
| `firstName` | `Имя` |
| `lastName` | `Фамилия` |
| `middleName` | `Отчество` |
| `email` | `Email` |
| `phone` | `Телефон` |

**Хешированные поля** (plaintext, используются для поиска):
| Поле | Колонка БД | Ограничения |
|------|-----------|-------------|
| `phoneHash` | `phone_hash` | `NOT NULL, UNIQUE` |
| `emailHash` | `email_hash` | — |

**Маскирование в `toString()`:**
```java
private String mask(String value) {
    if (value == null || value.length() < 3) return value;
    return "***" + value.substring(value.length() - 3);
}
```
В логах телефон/email отображаются как `***505`.

---

### 3.2 `src/main/java/by/urbash_hair/entity/AuditLog.java`

Сущность **не шифруется**, но является частью системы защиты данных:
- Фиксирует действия с персональными данными (`VIEW_CLIENT_PII`, `UPDATE_CLIENT`).
- Поля: `userId`, `action`, `details`, `timestamp`.
- Позволяет отслеживать, кто и когда обращался к ПДн.

---

## 4. Где и как используется шифрование/хеширование

### 4.1 Аутентификация (`PhoneAuthController.java`)

При входе по SMS:
1. Пользователь вводит телефон.
2. Система вычисляет `phoneHash = hashUtils.hashPhone(phone)`.
3. Ищет клиента по `phoneHash` (`clientRepository.findByPhoneHash(phoneHash)`).
4. Если клиент не найден — создаётся новый с заполненным `phone` (будет зашифрован автоматически JPA-конвертером) и `phoneHash`.

```java
String phoneHash = hashUtils.hashPhone(phone);
Client client = clientRepository.findByPhoneHash(phoneHash)
    .orElseGet(() -> {
        Client newClient = Client.builder()
            .phone(phone)          // ← автоматически зашифруется
            .phoneHash(phoneHash)  // ← хранится открыто (слепой индекс)
            .build();
        return clientRepository.save(newClient);
    });
```

### 4.2 Spring Security (`CustomUserDetailsService.java`)

При каждой проверке аутентификации:
```java
public UserDetails loadUserByUsername(String phone) {
    String phoneHash = hashUtils.hashPhone(phone);
    Client client = clientRepository.findByPhoneHash(phoneHash)
        .orElseThrow(...);
    // ...
}
```

### 4.3 Обновление профиля (`UserController.java`)

При изменении email:
```java
if (request.getEmail() != null && !request.getEmail().isEmpty()) {
    client.setEmail(request.getEmail());              // ← зашифруется
    client.setEmailHash(hashUtils.hashEmail(request.getEmail())); // ← хеш для поиска
}
```

Также фиксируется аудит:
```java
auditLogService.log(clientId, "UPDATE_CLIENT", "Client updated profile");
```

### 4.4 Создание заявок (`AppointmentService`, `CourseApplicationService`, `JobApplicationService`)

Во всех сервисах, где создаётся клиент по телефону/email:
1. Вычисляются `phoneHash` и `emailHash`.
2. Поиск существующего клиента по `phoneHash`.
3. При создании нового клиента `phone`/`email` попадают в зашифрованные поля, а хеши — в открытые.

---

## 5. Итоговая схема защиты данных клиента

```
┌─────────────────────────────────────────────────────────────┐
│                         Client Entity                        │
├─────────────────────────────────────────────────────────────┤
│  firstName, lastName, middleName, email, phone              │
│     ↓ @Convert(PersonalDataEncryptor)                      │
│     AES-256-GCM → Base64 → хранится в MySQL                 │
├─────────────────────────────────────────────────────────────┤
│  phoneHash, emailHash                                       │
│     ↓ HashUtils.sha256(значение + соль)                    │
│     Хранятся открыто, используются для SELECT/UNIQUE        │
├─────────────────────────────────────────────────────────────┤
│  JWT-токен (access/refresh)                                 │
│     ↓ HMAC-SHA256, подписан секретным ключом               │
│     Передаётся клиенту, содержит ID и роль                  │
├─────────────────────────────────────────────────────────────┤
│  AuditLog                                                   │
│     ↓ Логирование просмотра/изменения ПДн                  │
│     Кто, когда и какое действие совершил                    │
└─────────────────────────────────────────────────────────────┘
```

---

## 6. Список задействованных файлов

| Файл | Роль в системе шифрования |
|------|---------------------------|
| `config/PersonalDataEncryptor.java` | AES-256-GCM шифрование/расшифровка полей |
| `config/HashUtils.java` | SHA-256 хеширование с солью (blind index) |
| `config/JwtService.java` | Генерация/валидация JWT (HMAC-SHA256) |
| `entity/Client.java` | Сущность с 5 зашифрованными и 2 хешированными полями |
| `entity/AuditLog.java` | Аудит действий с ПДн |
| `repository/ClientRepository.java` | Поиск по `phoneHash`/`emailHash` |
| `controller/PhoneAuthController.java` | Хеширование телефона при входе/регистрации |
| `controller/UserController.java` | Хеширование email при обновлении профиля |
| `service/CustomUserDetailsService.java` | Хеширование телефона для Spring Security |
| `service/AppointmentService.java` | Хеширование при создании клиента из заявки |
| `service/CourseApplicationService.java` | Хеширование при создании клиента из заявки |
| `service/JobApplicationService.java` | Хеширование при создании клиента из заявки |
| `resources/application.yml` | Конфигурация переменных окружения для секретов |

