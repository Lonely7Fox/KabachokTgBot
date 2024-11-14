package io.project.KabachokTgBot.service.telegramBot;

import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandScopeAllChatAdministrators;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandScopeAllGroupChats;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandScopeAllPrivateChats;
import com.pengrad.telegrambot.request.SetMyCommands;

import java.util.ArrayList;
import java.util.List;

import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.ALL_STATS;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.PLAYER_LIST;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.PLAYER_STATS;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.PLAY_GAME;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.REGISTRATION;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.RULES;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.THIS_MONTH_STATS;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.TODAY_HOLIDAYS;
import static io.project.KabachokTgBot.service.telegramBot.TelegramBotConst.WEEKEND;

public class TelegramBotCommands {

    public static List<SetMyCommands> getCommands() {
        BotCommand registration = new BotCommand(REGISTRATION, "register for POTD game");
        BotCommand playGame = new BotCommand(PLAY_GAME, "play the game, see /pidorules first");
        BotCommand allStats = new BotCommand(ALL_STATS, "POTD game stats for all time");
        BotCommand rules = new BotCommand(RULES, "POTD game rules");
        BotCommand thisMonthStats = new BotCommand(THIS_MONTH_STATS, "POTD game stats for this month");
        BotCommand playerStats = new BotCommand(PLAYER_STATS, "POTD game personal stats");
        BotCommand playerList = new BotCommand(PLAYER_LIST, "show list of POTD chat players");
        BotCommand todayHolidays = new BotCommand(TODAY_HOLIDAYS, "list today holidays");
        BotCommand weekend = new BotCommand(WEEKEND, "days to weekend");

        //group chats
        SetMyCommands groupCommands = new SetMyCommands(registration, playGame, allStats, rules, thisMonthStats, playerStats, todayHolidays, weekend);
        groupCommands.scope(new BotCommandScopeAllGroupChats());

        //private chats
        SetMyCommands privateCommands = new SetMyCommands(rules, todayHolidays, weekend);
        groupCommands.scope(new BotCommandScopeAllPrivateChats());

        //group chat admins: group chat extended
        SetMyCommands adminCommands = new SetMyCommands(registration, playGame, allStats, rules, thisMonthStats, playerStats, playerList);
        groupCommands.scope(new BotCommandScopeAllChatAdministrators());

        List<SetMyCommands> myCommands = new ArrayList<>();
        myCommands.add(groupCommands);
        myCommands.add(privateCommands);
        myCommands.add(adminCommands);

        return myCommands;
    }
}
