package io.project.KabachokTgBot.utils;

import jdk.jfr.Experimental;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

public class TimeUtils {

    /* Для будущих доработок, по выставлению времени, если потребуется */
    public static final ZoneId zoneId = ZoneId.systemDefault();

    public static Timestamp now() {
        return Timestamp.from(Instant.now());
    }

    public static LocalDate todayLocalDate() {
        return LocalDate.now();
    }

    @Deprecated
    public static Date todayDate() {
        return Date.from(Instant.now());
    }

    public static boolean checkToday(Timestamp timestamp) {
        Instant time = timestamp.toInstant();
        return time.isAfter(startDayTime()) && time.isBefore(endDayTime());
    }

    public static Optional<Instant> checkAndGetDurationToEndDay(Timestamp timestamp) {
        return checkToday(timestamp) ? Optional.of(timestamp.toInstant()) : Optional.empty();
    }

    public static String getFormattedDuration(Instant time) {
        Duration duration = Duration.between(time, endDayTime());
        // Получаем количество дней, часов и минут
        long hours = duration.toHours() % 24; // Остаток от деления на 24 для часов
        long minutes = duration.toMinutes() % 60; // Остаток от деления на 60 для минут
        // Форматируем вывод
        return hours == 0 ? String.format("%d минут(ы)", minutes) : String.format("%d час(а) %d минут(ы)", hours, minutes);
    }

    public static boolean checkThisMonth(Timestamp timestamp) {
        LocalDate startMonthDay = todayLocalDate().withDayOfMonth(1);
        ZonedDateTime start = startMonthDay.atStartOfDay(zoneId);
        ZonedDateTime end = startMonthDay.plusMonths(1).atStartOfDay(zoneId);
        Instant time = timestamp.toInstant();

        return time.isAfter(start.toInstant()) && time.isBefore(end.toInstant());
    }

    public static String getRusMonthName() {
        Month month = todayLocalDate().getMonth();
        String[] months = new String[] {"null", "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
                "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
        //return month.getDisplayName(TextStyle.FULL, new Locale("ru"));
        return months[month.getValue()];
    }

    public static Instant startDayTime() {
        LocalDate today = todayLocalDate();
        LocalDateTime startOfDay = today.atStartOfDay();
        return startOfDay.atZone(zoneId).toInstant();
    }

    public static Instant endDayTime() {
        LocalDateTime endOfDay = todayLocalDate().atTime(23, 59, 59);
        return endOfDay.atZone(zoneId).toInstant();
    }

    public static long daysBetween(Date startDate, Date endDate) {
        Calendar start = setupNewCalendar(startDate);
        Calendar end = setupNewCalendar(endDate);

        long daysBetween = 0;
        while (start.before(end)) {
            start.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return daysBetween;
    }

    private static Calendar setupNewCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // Сбрасываем время для точности в вычислении дней
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
