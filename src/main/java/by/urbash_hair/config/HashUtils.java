package by.urbash_hair.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class HashUtils {

    private static final String PHONE_PREFIX = "phone";
    private static final String EMAIL_PREFIX = "email";

    private final String salt;

    public HashUtils(@Value("${app.security.phone-hash-salt}") String salt) {
        this.salt = salt;
    }

    public String hashPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        String normalized = normalizePhone(phone);
        return sha256(PHONE_PREFIX + ":" + normalized + ":" + salt);
    }

    public String hashEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String normalized = normalizeEmail(email);
        return sha256(EMAIL_PREFIX + ":" + normalized + ":" + salt);
    }

    private String normalizePhone(String phone) {
        return phone.replaceAll("[^\\d+]", "").trim();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}

