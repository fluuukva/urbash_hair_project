package by.urbash_hair.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyCodeRequest {
    @NotBlank(message = "Номер телефона обязателен")
    private String phone;

    @NotBlank(message = "Код подтверждения обязателен")
    private String code;

    private boolean consentGiven;

    private String firstName;
    private String lastName;
    private String middleName;
    private String email;

    // Способ доставки, который использовался при отправке кода
    @NotBlank(message = "Способ доставки обязателен")
    private String deliveryMethod;

    // Если пользователь выбрал TELEGRAM и хочет сохранить Telegram ID
    private String telegramId;

    // Способ, который пользователь предпочитает для будущих входов
    private String preferredDelivery;
}