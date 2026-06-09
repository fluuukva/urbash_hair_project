package by.urbash_hair.entity;

import by.urbash_hair.config.PersonalDataEncryptor;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "клиент")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Клиента")
    private Long id;

    @Convert(converter = PersonalDataEncryptor.class)
    @Column(name = "Имя")
    private String firstName;

    @Convert(converter = PersonalDataEncryptor.class)
    @Column(name = "Фамилия")
    private String lastName;

    @Convert(converter = PersonalDataEncryptor.class)
    @Column(name = "Отчество")
    private String middleName;

    @Convert(converter = PersonalDataEncryptor.class)
    @Column(name = "Email")
    private String email;

    @Convert(converter = PersonalDataEncryptor.class)
    @Column(name = "Телефон")
    private String phone;

    @Column(name = "зашифрованный_телефон", nullable = false, unique = true)
    private String phoneHash;

    @Column(name = "зашифрованный_email")
    private String emailHash;

    // Новые поля для двухфакторной аутентификации
    @Column(name = "telegram_id")
    private String telegramId;  // ID чата в Telegram или username

    @Column(name = "способ_доставки_кода")
    private String preferredDelivery; // "SMS", "EMAIL", "TELEGRAM"

    @Builder.Default
    @Column(name = "согласие_на_обработку_данных")
    private Boolean dataProcessingConsent = false;

    @Column(name = "дата_согласия")
    private LocalDateTime consentGivenAt;


    @OneToMany(mappedBy = "client")
    @JsonIgnoreProperties({"client"})
    private List<Appointment> appointments;

    @Override
    public String toString() {
        return "Client{id=" + id + ", phone=" + mask(phone) + ", email=" + mask(email) + "}";
    }

    private String mask(String value) {
        if (value == null || value.length() < 3) return value;
        return "***" + value.substring(value.length() - 3);
    }
}