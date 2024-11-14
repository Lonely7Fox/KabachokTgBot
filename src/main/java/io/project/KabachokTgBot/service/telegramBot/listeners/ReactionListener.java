package io.project.KabachokTgBot.service.telegramBot.listeners;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.reaction.ReactionTypeEmoji;
import com.pengrad.telegrambot.request.SetMessageReaction;
import io.project.KabachokTgBot.service.telegramBot.TelegramBotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReactionListener implements TelegramUpdateListener {
    private final Logger log = LoggerFactory.getLogger(ReactionListener.class);
    private final TelegramBotService service;
    private final HashMap<ArrayList<String>, ReactionTypeEmoji> emojiMap;

    public ReactionListener(TelegramBotService service) {
        this.service = service;
        this.emojiMap = fillEmojiMap();
    }

    private HashMap<ArrayList<String>, ReactionTypeEmoji> fillEmojiMap() {
        ArrayList<String> gayConst = new ArrayList<>(List.of("пидор", "пидр", "пидар"));
        ArrayList<String> squashConst = new ArrayList<>(List.of("кабачок", "кабачк"));
        ArrayList<String> poopConst = new ArrayList<>(List.of("говно", "срат", "срал", "серил", "кака", "срань"));
        ArrayList<String> planeConst = new ArrayList<>(List.of("беспилотник", "самолет", "самолёт"));
        ArrayList<String> yellowConst = new ArrayList<>(List.of("желтый"));
        ArrayList<String> kfcConst = new ArrayList<>(List.of("кфс", "kfc", "кфц"));
        ArrayList<String> beerConst = new ArrayList<>(List.of("пиво", "пит"));
        ArrayList<String> sofaConst = new ArrayList<>(List.of("диван", "кроват"));
        ArrayList<String> rollingConst = new ArrayList<>(List.of("подкру"));
        ArrayList<String> snowConst = new ArrayList<>(List.of("зима", "снег", "новый год", "новым годом"));
        ArrayList<String> dateConst = new ArrayList<>(List.of("праздник", "рождения", "рожденья"));

        HashMap<ArrayList<String>, ReactionTypeEmoji> emojiMap = new HashMap<>();
        emojiMap.put(gayConst, new ReactionTypeEmoji("🫡")); //SALUTING FACE (https://fileformat.info/info/unicode/char/1FAE1)
        emojiMap.put(squashConst, new ReactionTypeEmoji("🍌")); //BANANA (https://fileformat.info/info/unicode/char/1F34C)
        emojiMap.put(poopConst, new ReactionTypeEmoji("💩")); //PILE OF POO (https://fileformat.info/info/unicode/char/1F4A9)
        emojiMap.put(planeConst, new ReactionTypeEmoji("🕊")); //DOVE OF PEACE (https://fileformat.info/info/unicode/char/1F54A)
        emojiMap.put(yellowConst, new ReactionTypeEmoji("🤔")); //THINKING FACE (http://www.fileformat.info/info/unicode/char/1F914)
        emojiMap.put(kfcConst, new ReactionTypeEmoji("🌚")); //NEW MOON WITH FACE (https://fileformat.info/info/unicode/char/1F31A)
        emojiMap.put(sofaConst, new ReactionTypeEmoji("🥱")); //YAWNING FACE (https://fileformat.info/info/unicode/char/1F971)
        emojiMap.put(rollingConst, new ReactionTypeEmoji("💯")); //HUNDRED POINTS SYMBOL (https://fileformat.info/info/unicode/char/1F4AF)
        emojiMap.put(beerConst, new ReactionTypeEmoji("🍾")); //BOTTLE WITH POPPING CORK (https://fileformat.info/info/unicode/char/1F37E)
        emojiMap.put(snowConst, new ReactionTypeEmoji("☃")); //SNOWMAN (http://www.fileformat.info/info/unicode/char/2603)
        emojiMap.put(dateConst, new ReactionTypeEmoji("🎉"));//PARTY POPPER (https://fileformat.info/info/unicode/char/1F389)
        return emojiMap;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.message() != null && update.message().text() != null) {
            Message message = update.message();
            long chatId = message.chat().id();
            final String messageText = message.text().toLowerCase(Locale.ROOT);
            int messageId = message.messageId();
            ReactionTypeEmoji emoji = checkReactionTypeEmoji(messageText);
            if (emoji != null) {
                log.info("ChatMessageToBot: chatId={}, msg={}", chatId, "react");
                SetMessageReaction request = new SetMessageReaction(chatId, messageId, emoji);
                service.execute(request);
            }
        }
    }

    //return first entry react value
    private @Nullable ReactionTypeEmoji checkReactionTypeEmoji(String message) {
        for (Map.Entry<ArrayList<String>, ReactionTypeEmoji> entry : emojiMap.entrySet()) {
            if (entry.getKey().stream().anyMatch(message::contains)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
