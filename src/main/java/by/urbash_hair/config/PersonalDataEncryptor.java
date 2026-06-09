package by.urbash_hair.config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
public class PersonalDataEncryptor implements AttributeConverter<String, String> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private static SecretKey secretKey;
    private static volatile boolean initialized = false;

    public static synchronized void initialize(String password) {
        if (initialized) {
            return;
        }
        byte[] keyBytes = password.getBytes(StandardCharsets.UTF_8);
        byte[] fixedKey = new byte[32];
        System.arraycopy(keyBytes, 0, fixedKey, 0, Math.min(keyBytes.length, fixedKey.length));
        secretKey = new SecretKeySpec(fixedKey, "AES");
        initialized = true;
    }

    public PersonalDataEncryptor() {
    }

    private void ensureInitialized() {
        if (!initialized || secretKey == null) {
            throw new IllegalStateException("PersonalDataEncryptor is not initialized. Call initialize(password) before using.");
        }
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        ensureInitialized();
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));

            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            byte[] combined = ByteBuffer.allocate(iv.length + encrypted.length)
                    .put(iv)
                    .put(encrypted)
                    .array();

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption error", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        ensureInitialized();
        try {
            byte[] combined = Base64.getDecoder().decode(dbData);
            ByteBuffer buffer = ByteBuffer.wrap(combined);

            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);

            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH, iv));

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Decryption error", e);
        }
    }
}
