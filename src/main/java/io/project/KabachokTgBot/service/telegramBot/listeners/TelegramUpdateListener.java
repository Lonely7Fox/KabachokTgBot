package io.project.KabachokTgBot.service.telegramBot.listeners;

import com.pengrad.telegrambot.model.Update;

public interface TelegramUpdateListener {

    void onUpdateReceived(Update update);
}
