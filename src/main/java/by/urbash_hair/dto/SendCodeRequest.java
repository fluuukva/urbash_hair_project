package by.urbash_hair.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendCodeRequest {
    @NotBlank(message = "Номер телефона обязателен")
    private String phone;

    // Способ доставки: "SMS", "EMAIL", "TELEGRAM"
    @NotBlank(message = "Способ доставки обязателен")
    private String deliveryMethod;

    // Необязательное поле, если пользователь выбрал TELEGRAM и хочет указать свой Telegram ID
    private String telegramId;

    // Необязательное поле, если пользователь выбрал EMAIL
    private String email;
}