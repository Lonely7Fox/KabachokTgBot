package io.project.KabachokTgBot.service.telegramBot.listeners;

import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import io.project.KabachokTgBot.service.isdayoff.IsDayOffService;
import io.project.KabachokTgBot.service.telegramBot.TelegramBotService;
import io.project.KabachokTgBot.service.todayHolidays.TodayHolidaysService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.ALL_STATS;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.PLAYER_LIST;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.PLAYER_STATS;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.PLAY_GAME;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.REGISTRATION;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.RULES;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.TEXT_RULES;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.THIS_MONTH_STATS;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.TODAY_HOLIDAYS;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.WEEKEND;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.getCommands;

public class CommandListener implements TelegramUpdateListener {
    private final Logger log = LoggerFactory.getLogger(CommandListener.class);
    private final TelegramBotService service;
    private final List<String> commands;
    private final TodayHolidaysService todayHolidaysService;
    private final IsDayOffService isDayOffService;

    public CommandListener(TelegramBotService service) {
        this.service = service;
        this.commands = getCommands();
        this.todayHolidaysService = new TodayHolidaysService();
        this.isDayOffService = new IsDayOffService();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.message() != null && update.message().text() != null) {
            Message message = update.message();
            long chatId = message.chat().id();
            final String messageText = message.text();
            if (commands.stream().anyMatch(messageText::startsWith)) {
                log.info("ChatMessageToBot: chatId={}, msg={}", chatId, messageText);
                String msg = messageText;
                if (messageText.contains("@")) {
                    msg = messageText.substring(0, messageText.indexOf("@"));
                }
                switch (msg) {
                    case REGISTRATION -> service.registerUser(message);
                    case PLAY_GAME -> service.startGame(chatId);
                    case ALL_STATS -> service.showAllStats(chatId);
                    case RULES -> service.sendMessage(chatId, TEXT_RULES, true);
                    case THIS_MONTH_STATS -> service.showMonthStats(chatId);
                    case PLAYER_STATS -> service.showPlayerStats(chatId, message.from().id());
                    case PLAYER_LIST -> service.showPidorList(chatId);
                    case TODAY_HOLIDAYS -> service.sendMessage(chatId, todayHolidaysService.getMessage(), true);
                    case WEEKEND -> service.sendMessage(chatId, isDayOffService.getMessage(), true);
                    //case "/pidorlist del" -> //pidorlist del idid
                    //default -> sendSilentMessage(chatId, "Пх'нглуи мглв'нафх Ктулху Р'льех вгах'нагл фхтагн");
                }
            }
        }
    }

}
