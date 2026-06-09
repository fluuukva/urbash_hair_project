package by.urbash_hair.service;

import by.urbash_hair.entity.Client;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SmsCodeService {
    private static final long CODE_EXPIRATION_SECONDS = 300; // 5 минут
    private final Map<String, CodeEntry> storage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    // Генерация 6-значного кода
    public String generateCode() {
        return String.format("%06d", random.nextInt(1000000));
    }

    // Сохранить код для ключа (обычно phone или email)
    public void saveCode(String key, String code) {
        storage.put(key, new CodeEntry(code, LocalDateTime.now()));
    }

    public boolean verifyCode(String key, String code) {
        CodeEntry entry = storage.get(key);
        if (entry == null) return false;
        if (entry.getCreatedAt().plusSeconds(CODE_EXPIRATION_SECONDS).isBefore(LocalDateTime.now())) {
            storage.remove(key);
            return false;
        }
        return entry.getCode().equals(code);
    }

    public void removeCode(String key) {
        storage.remove(key);
    }

    // Отправка кода через SMS (заглушка, в реальности вызвать API провайдера)
    public boolean sendSms(String phone, String code) {
        System.out.println("=== SMS отправка ===");
        System.out.println("Номер: " + phone);
        System.out.println("Код: " + code);
        // Здесь будет реальный вызов SMS API
        // Например, через smscentre.by или Twilio
        return true; // заглушка всегда успешна
    }

    // Отправка кода на email
    public boolean sendEmail(String email, String code) {
        System.out.println("=== Email отправка ===");
        System.out.println("Email: " + email);
        System.out.println("Код: " + code);
        // Здесь будет реальная отправка через JavaMailSender
        return true;
    }

    // Отправка кода в Telegram
    public boolean sendTelegram(String chatId, String code) {
        System.out.println("=== Telegram отправка ===");
        System.out.println("ChatId: " + chatId);
        System.out.println("Код: " + code);
        // Здесь будет вызов Telegram Bot API
        return true;
    }

    // Основной метод отправки кода клиенту с учётом предпочтений
    public boolean sendCodeToClient(Client client, String deliveryMethod, String code) {
        switch (deliveryMethod.toUpperCase()) {
            case "SMS":
                if (client.getPhone() != null && !client.getPhone().isEmpty()) {
                    return sendSms(client.getPhone(), code);
                }
                break;
            case "EMAIL":
                if (client.getEmail() != null && !client.getEmail().isEmpty()) {
                    return sendEmail(client.getEmail(), code);
                }
                break;
            case "TELEGRAM":
                if (client.getTelegramId() != null && !client.getTelegramId().isEmpty()) {
                    return sendTelegram(client.getTelegramId(), code);
                }
                break;
        }
        return false;
    }

    private static class CodeEntry {
        private final String code;
        private final LocalDateTime createdAt;

        public CodeEntry(String code, LocalDateTime createdAt) {
            this.code = code;
            this.createdAt = createdAt;
        }

        public String getCode() { return code; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}