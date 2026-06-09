package by.urbash_hair.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class TelegramPollingBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    private final ConcurrentHashMap<String, Long> usernameToChatId = new ConcurrentHashMap<>();

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();
            String username = update.getMessage().getFrom().getUserName();
            if (username == null || username.isBlank()) {
                sendMessage(chatId, "У вас не установлен username. Пожалуйста, установите его в настройках Telegram и повторите команду /start.");
                return;
            }
            if ("/start".equals(text)) {
                usernameToChatId.put("@" + username, chatId);
                sendMessage(chatId, "✅ Ваш username @" + username + " сохранён. Теперь вы можете получать коды подтверждения на сайте.");
                System.out.println("Сохранён пользователь: @" + username + " → " + chatId);
            }
        }
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void sendVerificationCode(Long chatId, String code) {
        sendMessage(chatId, "Ваш код подтверждения для сайта URBASH.hair: " + code + "\n\nКод действителен 5 минут.");
    }

    public Long getChatIdByUsername(String username) {
        return usernameToChatId.get(username);
    }
}