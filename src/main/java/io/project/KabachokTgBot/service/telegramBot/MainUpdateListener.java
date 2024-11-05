package io.project.KabachokTgBot.service.telegramBot;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import io.project.KabachokTgBot.service.telegramBot.listeners.CommandListener;
import io.project.KabachokTgBot.service.telegramBot.listeners.ReactionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MainUpdateListener implements UpdatesListener {
    private final Logger log = LoggerFactory.getLogger(MainUpdateListener.class);
    private final TelegramBotService service;
    private final CommandListener commandListener;
    private final ReactionListener reactionListener;

    public MainUpdateListener(TelegramBotService service) {
        this.service = service;
        this.commandListener = new CommandListener(service);
        this.reactionListener = new ReactionListener(service);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            commandListener.onUpdateReceived(update);
            reactionListener.onUpdateReceived(update);
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }


//    @Override
//    public int process(List<Update> updates) {
//        updates.forEach(update -> {
//            if (update.message() != null && update.message().text() != null) {
//                Message message = update.message();
//                long chatId = message.chat().id();
//                String messageText = message.text();
//                SendResponse response = null;
//                if (update.message().text().startsWith("/hello")) {
//                    String msg = String.format("Hello, %s!", name);
//                    response = telegramBot.execute(new SendMessage(chatId, msg));
//                }
//                if (response != null) {
//                    logger.info("Processing answer: {}", response.message().text());
//                }
//            }
//        });
//        return UpdatesListener.CONFIRMED_UPDATES_ALL;
//    }

    //    private final ExceptionHandler exceptionHandler = e -> {
//        if (e.response() != null) {
//            // got bad response from telegram
//            e.response().errorCode();
//            e.response().description();
//        } else {
//            // probably network error
//            e.printStackTrace();
//        }
//    };
}
