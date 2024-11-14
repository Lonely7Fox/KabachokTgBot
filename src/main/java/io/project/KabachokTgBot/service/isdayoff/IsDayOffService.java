package io.project.KabachokTgBot.service.isdayoff;

import io.project.KabachokTgBot.logback.CacheDirProperty;
import io.project.KabachokTgBot.service.isdayoff.enums.DayType;
import io.project.KabachokTgBot.service.isdayoff.enums.DirectionType;
import io.project.KabachokTgBot.service.isdayoff.enums.LocalesType;
import io.project.KabachokTgBot.utils.TimeUtils;
import java.time.temporal.ChronoUnit;

import java.time.Duration;
import java.util.Date;

public class IsDayOffService {

    private final IsDayOff isDayOff;

    public IsDayOffService() {
        isDayOff = IsDayOff.Builder()
                .setLocale(LocalesType.RUSSIA)
                .setCacheDir(CacheDirProperty.CACHE_DIR)
                .setCacheStorageDays(7)
                .build();
    }

    public String get() {
        Date date = isDayOff.getFirstDayByType(TimeUtils.todayDate(), DayType.NOT_WORKING_DAY, DirectionType.FUTURE);
        long days = Duration.between(TimeUtils.todayDate().toInstant(), date.toInstant()).toDays();
        return getStats(days);
    }

    private String getStats(long days) {
        if (days == 0) {
            return "Пей пива, сегодня выходной!";
        }
        if (days == 1) {
            return "До ближайшего выходного - день, осталось еще чуть чуть!";
        }
        return "До ближайшего выходного - %d дня! Солнце еще высоко!".formatted(days);
    }
}
