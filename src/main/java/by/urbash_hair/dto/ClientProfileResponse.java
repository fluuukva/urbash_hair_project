package by.urbash_hair.dto;

import by.urbash_hair.entity.Client;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClientProfileResponse {

    private Long id;
    private String maskedPhone;
    private String maskedName;
    private String maskedEmail;
    private boolean dataProcessingConsent;

    public static ClientProfileResponse fromClient(Client client) {
        if (client == null) {
            return null;
        }
        return ClientProfileResponse.builder()
                .id(client.getId())
                .maskedPhone(maskPhone(client.getPhone()))
                .maskedName(maskName(client.getFirstName()))
                .maskedEmail(maskEmail(client.getEmail()))
                .dataProcessingConsent(client.getDataProcessingConsent() != null ? client.getDataProcessingConsent() : false)
                .build();
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 3) {
            return phone;
        }
        String lastThree = phone.substring(phone.length() - 3);
        // Формат: +7 (***) ***-XXX (последние 3 цифры)
        return "+7 (***) ***-" + lastThree;
    }

    private static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        return name.charAt(0) + "***";
    }

    private static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email;
        }
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (local.length() == 1) {
            return local + "***" + domain;
        }
        return local.charAt(0) + "***" + domain;
    }

    public static String maskFullName(String firstName, String lastName) {
        StringBuilder sb = new StringBuilder();
        if (lastName != null && !lastName.isEmpty()) {
            sb.append(maskName(lastName));
        }
        if (firstName != null && !firstName.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(maskName(firstName));
        }
        return sb.toString();
    }
}

