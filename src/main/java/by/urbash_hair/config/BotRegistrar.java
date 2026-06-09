package by.urbash_hair.config;

import by.urbash_hair.service.TelegramPollingBot;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@RequiredArgsConstructor
public class BotRegistrar {

    private final TelegramPollingBot telegramPollingBot;

    @PostConstruct
    public void registerBot() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramPollingBot);
            System.out.println("Telegram bot registered successfully.");
        } catch (TelegramApiException e) {
            System.err.println("Failed to register telegram bot: " + e.getMessage());
            e.printStackTrace();
        }
    }
}