package io.project.KabachokTgBot.utils.telegramBot.phrase;

import java.util.concurrent.ConcurrentHashMap;

public class LastUsedPhraseCache {

    private final ConcurrentHashMap<Long, Integer> firstPhraseIdCache;
    private final ConcurrentHashMap<Long, Integer> lastPhraseIdCache;

    public LastUsedPhraseCache() {
        firstPhraseIdCache = new ConcurrentHashMap<>();
        lastPhraseIdCache = new ConcurrentHashMap<>();
    }

    public Integer getFirstPhraseId(Long chatId) {
        return firstPhraseIdCache.get(chatId);
    }

    public Integer getLastPhraseId(Long chatId) {
        return lastPhraseIdCache.get(chatId);
    }

    public void setFirstPhraseId(Long chatId, Integer phraseId) {
        firstPhraseIdCache.put(chatId, phraseId);
    }

    public void setLastPhraseId(Long chatId, Integer phraseId) {
        lastPhraseIdCache.put(chatId, phraseId);
    }

}
