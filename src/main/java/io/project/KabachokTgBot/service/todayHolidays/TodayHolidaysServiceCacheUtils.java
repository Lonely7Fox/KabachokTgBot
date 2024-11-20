package io.project.KabachokTgBot.service.todayHolidays;

import io.project.KabachokTgBot.logback.CacheDirProperty;
import io.project.KabachokTgBot.utils.FileUtils;
import io.project.KabachokTgBot.utils.TimeUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static io.project.KabachokTgBot.utils.TimeUtils.ddMMMMRuPattern;

public class TodayHolidaysServiceCacheUtils {

    private static final Path cacheFilePath = Path.of(CacheDirProperty.CACHE_DIR).resolve("holidays.txt");

    public static boolean checkCache() {
        File cacheFile = new File(cacheFilePath.toAbsolutePath().toString());
        if (!cacheFile.exists()) {
            return false;
        } else {
            String todayDate = TimeUtils.todayLocalDate().format(ddMMMMRuPattern);
            return getCachedMessage().contains(todayDate);
        }
    }

    public static String getCachedMessage() {
        byte[] bytes = FileUtils.readBytesFromFile(cacheFilePath);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void setCachedMessage(String result) {
        FileUtils.writeToFile(result.getBytes(), cacheFilePath);
    }
}
