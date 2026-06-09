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
    private final TelegramPollingBot telegramPollingBot;   // используем polling бота

    @Value("${app.allow.delivery:all}")
    private String allowDelivery;

    @PostMapping("/send-code")
    public String sendCode(@Valid @RequestBody SendCodeRequest request) {
        String phone = request.getPhone();
        String deliveryMethod = request.getDeliveryMethod();
        String code = String.format("%06d", new Random().nextInt(999999));

        String phoneHash = hashUtils.hashPhone(phone);
        Client client = clientRepository.findByPhoneHash(phoneHash).orElse(null);
        if (client == null) {
            client = Client.builder()
                    .phone(phone)
                    .phoneHash(phoneHash)
                    .build();
        }

        String key = phone + ":" + deliveryMethod;
        smsCodeService.saveCode(key, code);

        switch (deliveryMethod) {
            case "SMS":
                System.out.println("=== ОТПРАВКА КОДА (SMS) ===");
                System.out.println("Номер: " + phone);
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
                Long chatId = telegramPollingBot.getChatIdByUsername(input);
                if (chatId == null) {
                    throw new RuntimeException("Не удалось найти пользователя по username: " + input +
                            ". Убедитесь, что вы отправили команду /start нашему боту @" + telegramPollingBot.getBotUsername() +
                            " и ваш username публичный.");
                }
                telegramPollingBot.sendVerificationCode(chatId, code);
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

        String key = phone + ":" + deliveryMethod;
        boolean valid = smsCodeService.verifyCode(key, code);
        if (!valid) {
            throw new RuntimeException("Неверный или просроченный код");
        }

        if (!request.isConsentGiven()) {
            throw new ConsentRequiredException("Необходимо дать согласие на обработку персональных данных");
        }

        String phoneHash = hashUtils.hashPhone(phone);
        Optional<Client> existingClient = clientRepository.findByPhoneHash(phoneHash);
        Client client;

        if (existingClient.isPresent()) {
            client = existingClient.get();
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
            if (Boolean.FALSE.equals(client.getDataProcessingConsent())) {
                client.setDataProcessingConsent(true);
                client.setConsentGivenAt(LocalDateTime.now());
            }
            client = clientRepository.save(client);
        } else {
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