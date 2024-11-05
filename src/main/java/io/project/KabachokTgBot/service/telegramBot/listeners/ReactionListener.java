package io.project.KabachokTgBot.service.telegramBot.listeners;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.reaction.ReactionTypeEmoji;
import com.pengrad.telegrambot.request.SetMessageReaction;
import io.project.KabachokTgBot.service.telegramBot.TelegramBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class ReactionListener implements TelegramUpdateListener {
    private final Logger log = LoggerFactory.getLogger(ReactionListener.class);
    private final TelegramBotService service;

    public ReactionListener(TelegramBotService service) {
        this.service = service;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.message() != null && update.message().text() != null) {
            Message message = update.message();
            long chatId = message.chat().id();
            final String messageText = message.text().toLowerCase(Locale.ROOT);
            int messageId = message.messageId();
            if (messageText.contains("пидор") || messageText.contains("пидр")) {
                log.info("ChatMessageToBot: chatId={}, msg={}", chatId, "pidorReact");
                SetMessageReaction request = new SetMessageReaction(chatId, messageId, new ReactionTypeEmoji("\uD83E\uDEE1"));
                service.execute(request);
            }
            if (messageText.contains("кабачок") || messageText.contains("кабачк")) {
                log.info("ChatMessageToBot: chatId={}, msg={}", chatId, "KabachokReact");
                SetMessageReaction request = new SetMessageReaction(chatId, messageId, new ReactionTypeEmoji("🍌"));
                service.execute(request);
            }
            if (messageText.contains("говно")) {
                log.info("ChatMessageToBot: chatId={}, msg={}", chatId, "PoopReact");
                SetMessageReaction request = new SetMessageReaction(chatId, messageId, new ReactionTypeEmoji("\uD83D\uDCA9"));
                service.execute(request);
            }
        }
    }

}
