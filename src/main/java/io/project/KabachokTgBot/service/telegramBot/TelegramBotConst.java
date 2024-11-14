package io.project.KabachokTgBot.service.telegramBot;

import java.util.ArrayList;
import java.util.List;

public class TelegramBotConst {

    public static final String REGISTRATION = "/pidoreg";
    public static final String PLAY_GAME = "/pidor";
    public static final String ALL_STATS = "/pidorstats";
    public static final String THIS_MONTH_STATS = "/pidormonth";
    public static final String PLAYER_STATS = "/pidorme";
    public static final String RULES = "/pidorules";
    public static final String PLAYER_LIST = "/pidorlist";
    public static final String TODAY_HOLIDAYS = "/todayholidays";
    public static final String WEEKEND = "/weekend";

    public static List<String> getCommands() {
        List<String> myCommands = new ArrayList<>();
        myCommands.add(REGISTRATION);
        myCommands.add(PLAY_GAME);
        myCommands.add(ALL_STATS);
        myCommands.add(RULES);
        myCommands.add(THIS_MONTH_STATS);
        myCommands.add(PLAYER_STATS);
        myCommands.add(PLAYER_LIST);
        myCommands.add(TODAY_HOLIDAYS);
        myCommands.add(WEEKEND);
        return myCommands;
    }
    
    public static final String TEXT_RULES =
            """
            Правила игры Пидор Дня (только для групповых чатов):
            1. Зарегистрируйтесь в игру по команде /pidoreg
            2. Подождите пока зарегиструются все (или большинство :)
            3. Запустите розыгрыш по команде /pidor
            4. Просмотр статистики канала по команде /pidorstats, /pidormonth
            5. Личная статистика по команде /pidorme
            6. (!!! Только для администраторов чатов): список игроков /pidorlist
            
            Важно, розыгрыш проходит только раз в день, повторная команда выведет результат игры.
            Сброс розыгрыша происходит каждый день в 12 часов ночи по Москве.
            """;

    public static final String TEXT_RULES_NEXT =
            """
            Правила игры Пидор Дня (только для групповых чатов):
            1. Зарегистрируйтесь в игру по команде /pidoreg
            2. Подождите пока зарегиструются все (или большинство :)
            3. Запустите розыгрыш по команде /pidor
            4. Просмотр статистики канала по команде /pidorstats, /pidormonth
            5. Личная статистика по команде /pidorme
            6. (!!! Только для администраторов чатов): удалить из игры может только Админ канала, сначала выведя по команде список игроков: /pidorlist
            Удалить же игрока можно по команде (используйте идентификатор пользователя - цифры из списка пользователей): /pidorlist del 123456
            
            Важно, розыгрыш проходит только раз в день, повторная команда выведет результат игры.
            
            Сброс розыгрыша происходит каждый день в 12 часов ночи по Москве.
            """;
}
