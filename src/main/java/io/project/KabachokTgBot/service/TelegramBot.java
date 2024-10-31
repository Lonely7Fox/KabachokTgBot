package io.project.KabachokTgBot.service;

import com.vdurmont.emoji.EmojiParser;
import io.project.KabachokTgBot.config.BotConfig;
import io.project.KabachokTgBot.model.potd.challenge.PotdChallenge;
import io.project.KabachokTgBot.model.potd.challenge.PotdChallengeRepository;
import io.project.KabachokTgBot.model.potd.chat.PotdChat;
import io.project.KabachokTgBot.model.potd.chat.PotdChatRepository;
import io.project.KabachokTgBot.model.potd.player.PotdPlayer;
import io.project.KabachokTgBot.model.potd.player.PotdPlayerRepository;
import io.project.KabachokTgBot.model.potd.telegramUser.PotdTelegramUser;
import io.project.KabachokTgBot.model.potd.telegramUser.PotdTelegramUserRepository;
import io.project.KabachokTgBot.scheduler.SimpleMessageJob;
import io.project.KabachokTgBot.scheduler.SystemScheduler;
import io.project.KabachokTgBot.utils.phrase.PhraseUtils;
import io.project.KabachokTgBot.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllChatAdministrators;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllGroupChats;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllPrivateChats;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private PotdTelegramUserRepository telegramUserRepository;

    @Autowired
    private PotdChatRepository chatRepository;

    @Autowired
    private PotdPlayerRepository playerRepository;

    @Autowired
    private PotdChallengeRepository challengeRepository;

    private final BotConfig config;
    private final PhraseUtils phraseUtils;
    private final SystemScheduler scheduler;

    private static final String RULES =
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

    private static final String RULES_NEXT =
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

    public TelegramBot(BotConfig botConfig) {
        super(botConfig.getToken());
        this.config = botConfig;
        this.phraseUtils = new PhraseUtils();

        this.scheduler = new SystemScheduler();
        scheduler.start();

        setupBotCommands();
    }

    private void setupBotCommands() {
        //group chats
        List<BotCommand> groupCommands = new ArrayList<>();
        groupCommands.add(new BotCommand("/pidoreg", "register for POTD game"));
        groupCommands.add(new BotCommand("/pidor", "play the game, see /pidorules first"));
        groupCommands.add(new BotCommand("/pidorstats", "POTD game stats for all time"));
        groupCommands.add(new BotCommand("/pidorules", "POTD game rules"));
        groupCommands.add(new BotCommand("/pidormonth", "POTD game stats for this month"));
        groupCommands.add(new BotCommand("/pidorme", "POTD game personal stats"));

        //private chats
        List<BotCommand> privateCommands = new ArrayList<>();
        privateCommands.add(new BotCommand("/pidorules", "POTD game rules"));

        //group chat admins: group chat extended
        List<BotCommand> adminCommands = new ArrayList<>(groupCommands);
        adminCommands.add(new BotCommand("/pidorlist", "show list of POTD chat players"));

        try {
            this.execute(new SetMyCommands(groupCommands, new BotCommandScopeAllGroupChats(), null));
            this.execute(new SetMyCommands(privateCommands, new BotCommandScopeAllPrivateChats(), null));
            this.execute(new SetMyCommands(adminCommands, new BotCommandScopeAllChatAdministrators(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String messageText = message.getText();
            long chatId = message.getChatId();
            log.info("ChatMessageToBot: chatId={}, msg={}", chatId, messageText);
            // /pidoreg@testingrandom777_bot , /pidoreg
            if (messageText.contains("@")) {
                messageText = messageText.substring(0, messageText.indexOf("@"));
            }
            switch (messageText) {
                case "/pidoreg" -> registerUser(message);
                case "/pidor" -> startGame(chatId);
                case "/pidorstats" -> showAllStats(chatId);
                case "/pidorules" -> sendSilentMessage(chatId, RULES);
                case "/pidormonth" -> showMonthStats(chatId);
                case "/pidorme" -> showPlayerStats(chatId, message.getFrom().getId());
                case "/pidorlist" -> showPidorList(chatId);
//                case "/pidorlist del" -> //pidorlist del idid
                default -> sendSilentMessage(chatId, "Пх'нглуи мглв'нафх Ктулху Р'льех вгах'нагл фхтагн");
            }
        }

    }

    //pidorme
    private void showPlayerStats(Long chatId, Long userId) {
        List<PotdChallenge> challengeList = getChallengeChatListByPlayer(chatId, userId);
        if (challengeList != null) {
            String msg = String.format("За все время - ты победитель номинации Пидор Дня - %s раз(а).\n", challengeList.size());
            sendSilentMessage(chatId, msg);
        } else {
            sendSilentMessage(chatId, "Статистика по тебе - отсутствует.");
        }
    }

    //pidormonth
    private void showMonthStats(long chatId) {
        List<PotdChallenge> challengeList = getChallengeChatMonthList(chatId);
        String month = TimeUtils.getRusMonthName(); //насрано в винительный падеж
        if (challengeList != null) {
            sendSilentMessage(chatId, getStatsMessage(challengeList, String.format("Топ-10 пидоров за %s:\n", month)));
        } else {
            sendSilentMessage(chatId, String.format("Статистика за %s отсутствует!", month));
        }
    }

    //pidorstats
    private void showAllStats(Long chatId) {
        Optional<PotdChat> chat = chatRepository.findById(chatId);
        if (chat.isPresent()) {
            List<PotdChallenge> challengeList = getChallengeChatList(chat.get());
            if (challengeList != null) {
                sendSilentMessage(chatId, getStatsMessage(challengeList, "Топ-10 пидоров за все время:\n"));
            }
        } else {
            sendSilentMessage(chatId, "Статистика отсутствует, вы еще не сыграли ни одной игры!");
        }
    }

    //pidorlist
    private void showPidorList(Long chatId) {
        Optional<PotdChat> chat = chatRepository.findById(chatId);
        if (chat.isPresent()) {
            List<PotdPlayer> list = getChatPlayersList(chat.get());
            if (list != null && !list.isEmpty()) {
                sendSilentMessage(chatId, getPlayersList(list));
            }
        } else {
            sendSilentMessage(chatId, "Еще никто не зарегистрирован! undefined");
        }
    }

    private List<PotdPlayer> getAllPlayersInfo() {
        return (List<PotdPlayer>) playerRepository.findAll();
    }

    private List<PotdChallenge> getChallengeInfo() {
        return (List<PotdChallenge>) challengeRepository.findAll();
    }

    private List<PotdChallenge> getChallengeChatList(PotdChat chat) {
        return getChallengeInfo().stream()
                .filter(list -> Objects.equals(list.getChat(), chat))
                .collect(Collectors.toList());
    }

    private List<PotdChallenge> getChallengeChatListByPlayer(Long chatId, Long userId) {
        return getChallengeInfo().stream()
                .filter(list -> Objects.equals(list.getChat().getId(), chatId) && Objects.equals(list.getPlayer().getUser().getId(), userId))
                .collect(Collectors.toList());
    }

    private List<PotdChallenge> getChallengeChatMonthList(Long chatId) {
        return getChallengeInfo().stream()
                .filter(list -> Objects.equals(list.getChat().getId(), chatId) && TimeUtils.checkThisMonth(list.getChallengeTime()))
                .collect(Collectors.toList());
    }

    private boolean checkTodayChallenge(PotdChat chat) {
        return getChallengeChatList(chat).stream()
                .anyMatch(ch -> TimeUtils.checkToday(ch.getChallengeTime()));
    }

    private @Nullable String checkTodayChallengeAndGetFormatTime(PotdChat chat) {
        for (PotdChallenge challenge : getChallengeChatList(chat)) {
            Optional<Timestamp> time = TimeUtils.checkAndGetDurationToEndDay(challenge.getChallengeTime());
            if (time.isPresent()) {
                return TimeUtils.getFormattedDuration(Duration.between(time.get().toInstant(), TimeUtils.endDayTime().toInstant()));
            }
        }
        return null;
    }

    private String getTodayChallengeWinner(PotdChat chat) {
        PotdChallenge challenge = getChallengeChatList(chat).stream()
                .filter(ch -> TimeUtils.checkToday(ch.getChallengeTime()))
                .limit(1)
                .toList()
                .get(0);
        return challenge.getPlayer().getUser().getUserName();
    }

    private List<PotdPlayer> getChatPlayersList(PotdChat chat) {
        return getAllPlayersInfo().stream()
                .filter(player -> Objects.equals(player.getChat(), chat) && player.getStatus().equals(true))
                .collect(Collectors.toList());
    }

    private @Nullable PotdPlayer getPlayer(PotdChat chat, PotdTelegramUser user) {
        for (PotdPlayer player : getAllPlayersInfo()) {
            if (Objects.equals(player.getChat(), chat) && Objects.equals(player.getUser(), user)) {
                return player;
            }
        }
        return null;
    }

    //pidor
    private void startGame(Long chatId) {
        Optional<PotdChat> chat = chatRepository.findById(chatId);
        if (chat.isEmpty()) {
            sendSilentMessage(chatId, "Еще никто не зарегистрирован, просьба проследовать к окошку");
            return;
        }
        PotdChat resChat = chat.get();
        String time = checkTodayChallengeAndGetFormatTime(resChat);
        if (time != null) {
            String str = String.format("\uD83D\uDE0E На сегодня пидор уже найден и это - @%s \uD83D\uDE0E \n Таймаут %s", getTodayChallengeWinner(resChat), time);
            sendSilentMessage(chatId, str);
            return;
        }
        //Я нашел пидора дня, но похоже, что он вышел из этого чата (вот пидор!), так что попробуйте еще раз!
        List<PotdPlayer> players = getChatPlayersList(resChat);
        if (players.isEmpty() || players.size() == 1) {
            if (players.isEmpty()) {
                sendSilentMessage(chatId, "Поиграть не получится, никто не зарегистрован :frowning_face: ");
            } else {
                sendSilentMessage(chatId, "Поиграть не получится, только один игрок зарегистрирован :frowning_face: ");
            }
            return;
        }
        Random random = new Random();
        int num = random.nextInt(0, players.size());

        PotdPlayer winner = players.get(num);
        long winnerId = winner.getId();

        Optional<PotdPlayer> player = playerRepository.findById(winnerId);
        if (player.isPresent()) {
            PotdChallenge challenge = new PotdChallenge();
            challenge.setPlayer(player.get());
            challenge.setChat(resChat);
            challenge.setChallengeTime(TimeUtils.now());
            challengeRepository.save(challenge);

            challengeActivity(chatId, player.get().getUser().getUserName());
            //sendMessage(chatId, ":banana: Победитель по жизни: @" + player.get().getUser().getUserName());
            return;
        }
        sendSilentMessage(chatId, "Что то пошло не так! :frowning_face:");
    }

    private void challengeActivity(Long chatId, String userName) {
        String first = phraseUtils.getRandomStartLine(chatId);
        String second = phraseUtils.getRandomStartLine(chatId);
        String third = phraseUtils.getRandomLastLine(chatId);

        sendSilentMessage(chatId, first);
        scheduleChatMessage(chatId, second, 3, true);
        scheduleChatMessage(chatId, third + userName, 6, false);
    }

    private void scheduleChatMessage(Long chatId, String message, int secInterval, boolean isSilent) {
        String name = UUID.randomUUID().toString();
        //заполняем джобу
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("telegramBot", this);
        jobDataMap.put("chatId", chatId);
        jobDataMap.put("message", message);
        jobDataMap.put("isSilent", isSilent);

        JobDetail job = JobBuilder.newJob(SimpleMessageJob.class)
                .withIdentity(name, "OnceMessageJob")
                .usingJobData(jobDataMap)
                .build();
        // Устанавливаем таймер
        Date triggerTime = DateBuilder.futureDate(secInterval, DateBuilder.IntervalUnit.SECOND);
        // Создаем триггер
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(name, "OnceMessageTrigger")
                .startAt(triggerTime)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    //pidoreg
    private void registerUser(Message msg) {
        Long chatId = msg.getChatId();
        Chat chat = msg.getChat();
        User user = msg.getFrom();
        //player and chat reg
        PotdTelegramUser regUser = addOrGetUser(user);
        PotdChat regChat = addOrGetChat(chat);
        //add chat with player into repo
        if (getPlayer(regChat, regUser) == null) {
            PotdPlayer potdPlayer = new PotdPlayer();
            potdPlayer.setChat(regChat);
            potdPlayer.setUser(regUser);
            potdPlayer.setStatus(true);
            playerRepository.save(potdPlayer);

            sendSilentMessage(chatId, ":wheelchair: Ты теперь участвуешь в игре \"Пидор Дня\" :wheelchair:");
            log.info("New POTD game player added: " + potdPlayer);
        } else {
            sendSilentMessage(chatId, "Эй, ты уже в игре! :crossed_swords: \nДважды в одну и туже реку, хех \uD83E\uDD1C \uD83D\uDC4C");
        }
    }

    private PotdTelegramUser addOrGetUser(User user) {
        Long userId = user.getId();
        Optional<PotdTelegramUser> tempUser = telegramUserRepository.findById(userId);
        if (tempUser.isEmpty()) {
            PotdTelegramUser player = new PotdTelegramUser();
            player.setId(userId);
            player.setName(user.getFirstName());
            player.setUserName(user.getUserName());
            player.setRegistredAt(TimeUtils.now());
            return telegramUserRepository.save(player);
        } else {
            return tempUser.get();
        }
    }

    private PotdChat addOrGetChat(Chat chat) {
        Long chatId = chat.getId();
        Optional<PotdChat> tempChat = chatRepository.findById(chatId);
        if (tempChat.isEmpty()) {
            PotdChat chatik = new PotdChat();
            chatik.setId(chatId);
            chatik.setName(chat.getTitle());
            chatik.setRegisteredAt(TimeUtils.now());
            return chatRepository.save(chatik);
        } else {
            return tempChat.get();
        }
    }

    private String getStatsMessage(List<PotdChallenge> challengeList, String startMsg) {
        //Топ-10 пидоров за все время:
        //
        //1. chipercu — 24 раз(а)
        //2. Lonely7Fox — 23 раз(а)
        //3. Александр Миронов — 18 раз(а)
        //4. llepceu — 14 раз(а)
        //5. dark1zz — 10 раз(а)
        //6. Евгений Радайкин — 10 раз(а)
        //7. dmitrybeloglazov — 7 раз(а)
        //8. tcilafof — 2 раз(а)
        //9. Falcon411 — 1 раз(а)
        //
        //Всего участников — 9
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(startMsg);
        stringBuilder.append("\n");

        Map<PotdPlayer, Integer> frequencyMap = new HashMap<>();
        for (PotdChallenge ch : challengeList) {
            PotdPlayer player = ch.getPlayer();
            frequencyMap.put(player, frequencyMap.getOrDefault(player, 0) + 1);
        }

        //Sort by frequency in descending order
        List<Map.Entry<PotdPlayer, Integer>> entries = new ArrayList<>(frequencyMap.entrySet());
        entries.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        int index = 1;
        for (Map.Entry<PotdPlayer, Integer> entry : entries) {
            if (index == 11) {
                break;
            }
            String text = String.format("%d. @%s - %d раз(а)\n", index, entry.getKey().getUser().getUserName(), entry.getValue());
            stringBuilder.append(text);
            index++;
        }
        stringBuilder.append("\n");
        stringBuilder.append("Всего участников — ");
        stringBuilder.append(entries.size());
        return stringBuilder.toString();
    }

    private String getPlayersList(List<PotdPlayer> list) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Участники из чата: ").append("\n");
        for (PotdPlayer p : list) {
            String format = String.format("@%s", p.getUser().getUserName());
            stringBuilder.append(format).append(", ");
        }
        String str = stringBuilder.toString();
        str = str.substring(0, str.length() - 2);
        return str;
    }


    /* with smiles https://github-emoji-picker.vercel.app/  https://gist.github.com/ricealexander/ae8b8cddc3939d6ba212f953701f53e6 */
    public void sendMessage(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(EmojiParser.parseToUnicode(textToSend));

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    public void sendSilentMessage(long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(EmojiParser.parseToUnicode(textToSend));
        sendMessage.disableNotification();

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    public BotConfig getConfig() {
        return config;
    }


    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Deprecated
    @Override
    public String getBotToken() {
        return config.getToken();
    }
}


