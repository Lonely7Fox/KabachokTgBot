package io.project.KabachokTgBot.utils;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class TimeUtils {

    private static final ZoneId zoneId = ZoneId.of("UTC+3");

    private static final ZoneOffset zoneOffset = ZoneOffset.of("+03:00");

    private static final Clock clock = Clock.system(zoneId);

    public static Timestamp now() {
        return Timestamp.from(Instant.now(clock));
    }

    public static LocalDate today() {
        return LocalDate.now(zoneId);
    }

    public static boolean checkToday(Timestamp timestamp) {
        return timestamp.after(startDayTime()) && timestamp.before(endDayTime());
    }

    public static boolean checkThisMonth(Timestamp timestamp) {
        LocalDate start = today().withDayOfMonth(1);
        LocalDate end = start.plusMonths(1);

        Timestamp timeStart = Timestamp.from(start.atStartOfDay(zoneId).toLocalDateTime().toInstant(zoneOffset));
        Timestamp timeEnd = Timestamp.from(end.atStartOfDay(zoneId).toLocalDateTime().toInstant(zoneOffset));

        return timestamp.after(timeStart) && timestamp.before(timeEnd);
    }

    public static String getRusMonthName() {
        Month month = today().getMonth();
        String[] months = new String[] {"null", "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
        //return month.getDisplayName(TextStyle.FULL, new Locale("ru"));
        return months[month.getValue()];
    }

    private static Timestamp startDayTime() {
        LocalDate today = today();
        LocalDateTime startOfDay = today.atStartOfDay(zoneId).toLocalDateTime();
        Instant startOfDayTimestamp = startOfDay.toInstant(zoneOffset);
        return Timestamp.from(startOfDayTimestamp);
    }

    private static Timestamp endDayTime() {
        LocalDateTime endOfDay = today().atTime(23, 59, 59);
        Instant endOfDayTimestamp = endOfDay.toInstant(zoneOffset);
        return Timestamp.from(endOfDayTimestamp);
    }


}
