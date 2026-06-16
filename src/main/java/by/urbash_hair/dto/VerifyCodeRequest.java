package by.urbash_hair.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyCodeRequest {
    // Телефон теперь НЕ обязателен
    private String phone;

    @NotBlank(message = "Код подтверждения обязателен")
    private String code;

    private boolean consentGiven;

    private String firstName;
    private String lastName;
    private String middleName;
    private String email;

    @NotBlank(message = "Способ доставки обязателен")
    private String deliveryMethod;

    private String telegramId;
    private String preferredDelivery;
}