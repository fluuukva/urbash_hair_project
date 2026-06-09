package by.urbash_hair.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("URBASH.hair: Код подтверждения");
        message.setText("Ваш код подтверждения: " + code + "\n\nКод действителен в течение 5 минут.");
        mailSender.send(message);
    }
}