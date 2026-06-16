package by.urbash_hair.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendCodeRequest {
    // Телефон теперь НЕ обязателен
    private String phone;

    @NotBlank(message = "Способ доставки обязателен")
    private String deliveryMethod;

    private String telegramId;
    private String email;
}