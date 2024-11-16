package io.project.KabachokTgBot.service.isdayoff;

import io.project.KabachokTgBot.logback.CacheDirProperty;
import io.project.KabachokTgBot.service.isdayoff.enums.DayType;
import io.project.KabachokTgBot.service.isdayoff.enums.DirectionType;
import io.project.KabachokTgBot.service.isdayoff.enums.LocalesType;
import io.project.KabachokTgBot.utils.TimeUtils;

import java.time.Instant;
import java.time.LocalDate;
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

    public String getMessage() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getStatsNearestWeekend());
        stringBuilder.append("\n");
        stringBuilder.append(getStatsNearNewYear());
        return stringBuilder.toString();
    }

    private long getFutureWeekendDay() {
        Date today = TimeUtils.todayDate();
        Date date = isDayOff.getFirstDayByType(today, DayType.NOT_WORKING_DAY, DirectionType.FUTURE);
        return TimeUtils.daysBetween(today, date);
    }

    private long getFutureWorkingDay() {
        Date today = TimeUtils.todayDate();
        Date date = isDayOff.getFirstDayByType(today, DayType.WORKING_DAY, DirectionType.FUTURE);
        return TimeUtils.daysBetween(today, date);
    }

    private String getStatsNearestWeekend() {
        long days = getFutureWeekendDay();
        StringBuilder result = new StringBuilder();
        result.append("🗿 Выходной ты где? 🗿\n");
        if (days == 0) {
            result.append(getStatsNearestWorkingDay());
        } else if (days == 1) {
            result.append("До ближайшего выходного - день, осталось еще чуть чуть!");
        } else {
            result.append("До ближайшего выходного - %d дня(-ей)! Солнце еще высоко!".formatted(days));
        }
        return result.append("\n").toString();
    }

    private String getStatsNearestWorkingDay() {
        StringBuilder result = new StringBuilder();
        long daysToWork = getFutureWorkingDay();
        if (daysToWork == 1) {
            result.append("Пей пива, сегодня выходной! Завтра на работу!");
        } else {
            result.append("Пей пива, сегодня выходной! На работу через %d дня(-ей)!".formatted(daysToWork));
        }
        return result.toString();
    }

    //christmas timer
    private String getStatsNearNewYear() {
        LocalDate today = TimeUtils.todayLocalDate();
        Instant newYearDate = LocalDate.of(today.getYear() + 1, 1, 1).atStartOfDay(TimeUtils.zoneId).toInstant();
        String date = TimeUtils.getFormattedDuration(TimeUtils.now(), newYearDate);
        return String.format("🎄 До Нового года осталось: 🎄\n%s", date);
    }
}
