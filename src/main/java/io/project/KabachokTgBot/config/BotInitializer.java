package io.project.KabachokTgBot.config;

import io.project.KabachokTgBot.service.telegramBot.TelegramBotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BotInitializer {

    @Autowired
    TelegramBotService bot;

    @EventListener({ContextRefreshedEvent.class})
    public void init() {
        bot.start();
    }
}
