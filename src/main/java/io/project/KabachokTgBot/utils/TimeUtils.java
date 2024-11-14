package io.project.KabachokTgBot.utils;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

public class TimeUtils {

    public static final ZoneId zoneId = ZoneId.of("UTC+3");

    public static final ZoneOffset zoneOffset = ZoneOffset.of("+03:00");

    public static final Clock clock = Clock.system(zoneId);

    public static Timestamp now() {
        return Timestamp.from(Instant.now(clock));
    }

    public static LocalDate todayLocalDate() {
        return LocalDate.now(zoneId);
    }

    public static Date todayDate() {
        return Date.from(Instant.now(clock));
    }

    public static boolean checkToday(Timestamp timestamp) {
        return timestamp.after(startDayTime()) && timestamp.before(endDayTime());
    }

    public static Optional<Timestamp> checkAndGetDurationToEndDay(Timestamp timestamp) {
//        if (timestamp.after(startDayTime()) && timestamp.before(endDayTime())) {
//            String duration = getFormattedDuration(Duration.between(timestamp.toInstant(), endDayTime().toInstant()));
//            return Optional.of(duration);
//        }
//        return Optional.empty();
        return timestamp.after(startDayTime()) && timestamp.before(endDayTime()) ? Optional.of(timestamp) : Optional.empty();
    }

    public static String getFormattedDuration(Duration duration) {
        // Получаем количество дней, часов и минут
        long hours = duration.toHours() % 24; // Остаток от деления на 24 для часов
        long minutes = duration.toMinutes() % 60; // Остаток от деления на 60 для минут
        // Форматируем вывод
        return String.format("%d:%d", hours, minutes);
    }

    public static boolean checkThisMonth(Timestamp timestamp) {
        LocalDate start = todayLocalDate().withDayOfMonth(1);
        LocalDate end = start.plusMonths(1);

        Timestamp timeStart = Timestamp.from(start.atStartOfDay(zoneId).toLocalDateTime().toInstant(zoneOffset));
        Timestamp timeEnd = Timestamp.from(end.atStartOfDay(zoneId).toLocalDateTime().toInstant(zoneOffset));

        return timestamp.after(timeStart) && timestamp.before(timeEnd);
    }

    public static String getRusMonthName() {
        Month month = todayLocalDate().getMonth();
        String[] months = new String[] {"null", "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
        //return month.getDisplayName(TextStyle.FULL, new Locale("ru"));
        return months[month.getValue()];
    }

    public static Timestamp startDayTime() {
        LocalDate today = todayLocalDate();
        LocalDateTime startOfDay = today.atStartOfDay(zoneId).toLocalDateTime();
        Instant startOfDayTimestamp = startOfDay.toInstant(zoneOffset);
        return Timestamp.from(startOfDayTimestamp);
    }

    public static Timestamp endDayTime() {
        LocalDateTime endOfDay = todayLocalDate().atTime(23, 59, 59);
        Instant endOfDayTimestamp = endOfDay.toInstant(zoneOffset);
        return Timestamp.from(endOfDayTimestamp);
    }

    public static long daysBetween(Date startDate, Date endDate) {
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        start.setTime(startDate);
        end.setTime(endDate);

        // Сбрасываем время для точности в вычислении дней
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);

        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);

        // Считаем дни
        long daysBetween = 0;
        while (start.before(end)) {
            start.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }

        return daysBetween;
    }
}
