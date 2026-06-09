package by.urbash_hair.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptorInitializer {

    @Value("${jasypt.encryptor.password}")
    private String password;

    @PostConstruct
    public void init() {
        PersonalDataEncryptor.initialize(password);
    }
}

