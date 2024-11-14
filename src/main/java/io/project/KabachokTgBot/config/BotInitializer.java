package io.project.KabachokTgBot.config;

import ch.qos.logback.core.util.FileUtil;
import io.project.KabachokTgBot.logback.CacheDirProperty;
import io.project.KabachokTgBot.service.telegramBot.TelegramBotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class BotInitializer {

    @Autowired
    TelegramBotService bot;

    @EventListener({ContextRefreshedEvent.class})
    public void init() {
        createCacheDir();
        bot.start();
    }

    private boolean createCacheDir() {
        String cacheDir = CacheDirProperty.CACHE_DIR;
        if (!cacheDir.endsWith("/") && !cacheDir.isEmpty()) {
            cacheDir += "/";
        }
        File cacheDirFile = new File(cacheDir);
        return createMissingDirectories(cacheDirFile);
    }

    public boolean createMissingDirectories(File parent) {
        if (parent == null) {
            return true;
        } else {
            parent.mkdirs();
            return parent.exists();
        }
    }
}
