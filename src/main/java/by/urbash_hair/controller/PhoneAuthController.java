package by.urbash_hair.controller;

import by.urbash_hair.config.HashUtils;
import by.urbash_hair.config.JwtService;
import by.urbash_hair.dto.AuthResponse;
import by.urbash_hair.dto.SendCodeRequest;
import by.urbash_hair.dto.VerifyCodeRequest;
import by.urbash_hair.entity.Client;
import by.urbash_hair.exception.ConsentRequiredException;
import by.urbash_hair.repository.ClientRepository;
import by.urbash_hair.service.EmailService;
import by.urbash_hair.service.SmsCodeService;
import by.urbash_hair.service.TelegramPollingBot;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PhoneAuthController {

    private final SmsCodeService smsCodeService;
    private final ClientRepository clientRepository;
    private final JwtService jwtService;
    private final HashUtils hashUtils;
    private final EmailService emailService;
    private final Optional<TelegramPollingBot> telegramPollingBot;

    @Value("${app.allow.delivery:all}")
    private String allowDelivery;

    @PostMapping("/send-code")
public String sendCode(@Valid @RequestBody SendCodeRequest request) {
    String deliveryMethod = request.getDeliveryMethod();
    String code = String.format("%06d", new Random().nextInt(999999));
    Client client = null;

    // Поиск клиента по email (для входа) или по телефону (для регистрации)
    if (request.getEmail() != null && !request.getEmail().isBlank()) {
        String emailHash = hashUtils.hashEmail(request.getEmail());
        client = clientRepository.findByEmailHash(emailHash).orElse(null);
    } else if (request.getPhone() != null && !request.getPhone().isBlank()) {
        String phoneHash = hashUtils.hashPhone(request.getPhone());
        client = clientRepository.findByPhoneHash(phoneHash).orElse(null);
    } else if (request.getTelegramId() != null && !request.getTelegramId().isBlank()) {
        client = clientRepository.findByTelegramId(request.getTelegramId()).orElse(null);
    }

    // Если клиент не найден, но передан телефон – создаём временного клиента (для регистрации)
    if (client == null && request.getPhone() != null && !request.getPhone().isBlank()) {
        client = Client.builder()
                .phone(request.getPhone())
                .phoneHash(hashUtils.hashPhone(request.getPhone()))
                .build();
    }

    if (client == null) {
        throw new RuntimeException("Пользователь не найден. Пожалуйста, зарегистрируйтесь.");
    }

    // Формируем ключ для хранения кода
    String key;
    if (request.getPhone() != null && !request.getPhone().isBlank()) {
        key = request.getPhone() + ":" + deliveryMethod;
    } else if (request.getEmail() != null && !request.getEmail().isBlank()) {
        key = request.getEmail() + ":" + deliveryMethod;
    } else {
        key = request.getTelegramId() + ":" + deliveryMethod;
    }
    smsCodeService.saveCode(key, code);

    switch (deliveryMethod) {
        case "SMS":
            System.out.println("=== ОТПРАВКА КОДА (SMS) ===");
            System.out.println("Номер: " + request.getPhone());
            System.out.println("Код: " + code);
            System.out.println("==========================");
            break;

        case "EMAIL":
            String emailTo = request.getEmail();
            if (emailTo == null || emailTo.isBlank()) {
                throw new RuntimeException("Для отправки кода на email укажите email");
            }
            emailService.sendVerificationCode(emailTo, code);
            break;

        case "TELEGRAM":
            String input = request.getTelegramId();
            if (input == null || input.isBlank()) {
                throw new RuntimeException("Укажите ваш Telegram username (начинается с @)");
            }
            if (telegramPollingBot.isEmpty()) {
                System.out.println("=== ТЕЛЕГРАМ БОТ НЕДОСТУПЕН (локальный режим) ===");
                System.out.println("Код для пользователя " + input + ": " + code);
                System.out.println("================================================");
                break;
            }
            Long chatId = telegramPollingBot.get().getChatIdByUsername(input);
            if (chatId == null) {
                System.out.println("=== ПОЛЬЗОВАТЕЛЬ НЕ НАЙДЕН В ТЕЛЕГРАМ ===");
                System.out.println("Username: " + input);
                System.out.println("Код: " + code);
                System.out.println("=========================================");
                break;
            }
            telegramPollingBot.get().sendVerificationCode(chatId, code);
            break;

        default:
            throw new RuntimeException("Неизвестный способ доставки: " + deliveryMethod);
    }

    return "Код отправлен на " + deliveryMethod;
}

    @PostMapping("/verify-code")
    public AuthResponse verifyCode(@Valid @RequestBody VerifyCodeRequest request) {
        String phone = request.getPhone();
        String code = request.getCode();
        String deliveryMethod = request.getDeliveryMethod();

        // Формируем ключ для проверки кода
        String key;
        if (phone != null && !phone.isBlank()) {
            key = phone + ":" + deliveryMethod;
        } else if (request.getEmail() != null && !request.getEmail().isBlank()) {
            key = request.getEmail() + ":" + deliveryMethod;
        } else {
            key = request.getTelegramId() + ":" + deliveryMethod;
        }
        boolean valid = smsCodeService.verifyCode(key, code);
        if (!valid) {
            throw new RuntimeException("Неверный или просроченный код");
        }

        String phoneHash = phone != null && !phone.isBlank() ? hashUtils.hashPhone(phone) : null;
        Client client = null;

        // Поиск клиента по телефону, email или telegramId
        if (phoneHash != null) {
            Optional<Client> existingClient = clientRepository.findByPhoneHash(phoneHash);
            if (existingClient.isPresent()) client = existingClient.get();
        }
        if (client == null && request.getEmail() != null && !request.getEmail().isBlank()) {
            String emailHash = hashUtils.hashEmail(request.getEmail());
            Optional<Client> existingClient = clientRepository.findByEmailHash(emailHash);
            if (existingClient.isPresent()) client = existingClient.get();
        }
        if (client == null && request.getTelegramId() != null && !request.getTelegramId().isBlank()) {
            Optional<Client> existingClient = clientRepository.findByTelegramId(request.getTelegramId());
            if (existingClient.isPresent()) client = existingClient.get();
        }

        if (client == null) {
            // Новый пользователь – создаём
            if (!request.isConsentGiven()) {
                throw new ConsentRequiredException("Необходимо дать согласие на обработку персональных данных");
            }
            Client newClient = Client.builder()
                    .phone(phone)
                    .phoneHash(phoneHash)
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .middleName(request.getMiddleName())
                    .email(request.getEmail())
                    .telegramId(request.getTelegramId())
                    .preferredDelivery(request.getPreferredDelivery())
                    .dataProcessingConsent(true)
                    .consentGivenAt(LocalDateTime.now())
                    .build();
            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                newClient.setEmailHash(hashUtils.hashEmail(request.getEmail()));
            }
            client = clientRepository.save(newClient);
        } else {
            // Существующий пользователь – обновляем
            if (Boolean.FALSE.equals(client.getDataProcessingConsent())) {
                if (!request.isConsentGiven()) {
                    throw new ConsentRequiredException("Необходимо дать согласие на обработку персональных данных");
                } else {
                    client.setDataProcessingConsent(true);
                    client.setConsentGivenAt(LocalDateTime.now());
                }
            }
            if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
                client.setFirstName(request.getFirstName());
            }
            if (request.getLastName() != null && !request.getLastName().isBlank()) {
                client.setLastName(request.getLastName());
            }
            if (request.getMiddleName() != null && !request.getMiddleName().isBlank()) {
                client.setMiddleName(request.getMiddleName());
            }
            if (request.getEmail() != null && !request.getEmail().isBlank()) {
                client.setEmail(request.getEmail());
                client.setEmailHash(hashUtils.hashEmail(request.getEmail()));
            }
            if (request.getTelegramId() != null && !request.getTelegramId().isBlank()) {
                client.setTelegramId(request.getTelegramId());
            }
            if (request.getPreferredDelivery() != null && !request.getPreferredDelivery().isBlank()) {
                client.setPreferredDelivery(request.getPreferredDelivery());
            }
            client = clientRepository.save(client);
        }

        smsCodeService.removeCode(key);

        String token = jwtService.generateToken(client);

        return AuthResponse.builder()
                .token(token)
                .id(client.getId())
                .lastName(client.getLastName())
                .firstName(client.getFirstName())
                .middleName(client.getMiddleName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .build();
    }
}