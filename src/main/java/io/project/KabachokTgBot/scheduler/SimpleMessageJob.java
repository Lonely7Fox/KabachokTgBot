package io.project.KabachokTgBot.scheduler;

import io.project.KabachokTgBot.service.TelegramBot;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Service;

@Service
public class SimpleMessageJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        TelegramBot telegramBot = (TelegramBot) dataMap.get("telegramBot");
        long chatId = dataMap.getLong("chatId");
        String message = dataMap.getString("message");

        telegramBot.sendMessage(chatId, message);
    }
}
