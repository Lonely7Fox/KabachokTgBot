package io.project.KabachokTgBot.service.telegramBot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
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
import io.project.KabachokTgBot.utils.telegramBot.phrase.PhraseUtils;
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

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.project.KabachokTgBot.utils.TimeUtils.endDayTime;
import static io.project.KabachokTgBot.utils.telegramBot.TelegramStatsUtils.getPlayersList;
import static io.project.KabachokTgBot.utils.telegramBot.TelegramStatsUtils.getStatsMessage;

@Slf4j
@Service
public class TelegramBotService {

    @Autowired
    private PotdTelegramUserRepository telegramUserRepository;

    @Autowired
    private PotdChatRepository chatRepository;

    @Autowired
    private PotdPlayerRepository playerRepository;

    @Autowired
    private PotdChallengeRepository challengeRepository;

    private final TelegramBot telegramBot;
    private final PhraseUtils phraseUtils;
    private final SystemScheduler scheduler;
    private final String botName;

    public TelegramBotService(BotConfig botConfig) {
        this.telegramBot = new TelegramBot(botConfig.getToken());
        this.botName = botConfig.getBotName();
        this.phraseUtils = new PhraseUtils();
        this.scheduler = new SystemScheduler();
    }

    public void start() {
        //Запуск планировщика
        scheduler.start();
        //Регистрация команд бота
        List<SetMyCommands> commands = TelegramBotCommands.getCommands();
        commands.forEach(this::execute);
        //подписка на обновления чата
        telegramBot.setUpdatesListener(new MainUpdateListener(this));
    }

    //pidoreg
    public void registerUser(Message msg) {
        Chat chat = msg.chat();
        Long chatId = chat.id();
        User user = msg.from();
        //player and chat reg
        PotdTelegramUser regUser = addOrGetUser(user);
        PotdChat regChat = addOrGetChat(chat);
        //add chat with player into repo
        if (getPlayer(regChat, regUser) == null) {
            PotdPlayer potdPlayer = saveNewPlayer(regChat, regUser);
            sendMessage(chatId, "♿ Ты теперь участвуешь в игре \"Пидор Дня\" ♿", true);
            log.info("New POTD game player added: " + potdPlayer);
        } else {
            sendMessage(chatId, "⚔ Эй, ты уже в игре! ⚔", true);
        }
    }

    //pidorme
    public void showPlayerStats(Long chatId, Long userId) {
        List<PotdChallenge> challengeList = getChallengeChatListByPlayer(chatId, userId);
        if (challengeList != null) {
            String msg = String.format("За все время - ты победитель номинации Пидор Дня - %s раз(а).\n", challengeList.size());
            sendMessage(chatId, msg, true);
        } else {
            sendMessage(chatId, "Статистика по тебе - отсутствует.", true);
        }
    }

    //pidormonth
    public void showMonthStats(long chatId) {
        List<PotdChallenge> challengeList = getChallengeChatMonthList(chatId);
        String month = TimeUtils.getRusMonthName(); //насрано в винительный падеж
        if (challengeList != null) {
            sendMessage(chatId, getStatsMessage(challengeList, String.format("Топ-10 пидоров за %s:\n", month)), true);
        } else {
            sendMessage(chatId, String.format("Статистика за %s отсутствует!", month), true);
        }
    }

    //pidorstats
    public void showAllStats(Long chatId) {
        Optional<PotdChat> chat = chatRepository.findById(chatId);
        if (chat.isPresent()) {
            List<PotdChallenge> challengeList = getChallengeChatList(chat.get());
            if (challengeList != null) {
                sendMessage(chatId, getStatsMessage(challengeList, "Топ-10 пидоров за все время:\n"), true);
            }
        } else {
            sendMessage(chatId, "Статистика отсутствует, вы еще не сыграли ни одной игры!", true);
        }
    }

    //pidorlist
    public void showPidorList(Long chatId) {
        Optional<PotdChat> chat = chatRepository.findById(chatId);
        if (chat.isPresent()) {
            List<PotdPlayer> list = getChatPlayersList(chat.get());
            if (list != null && !list.isEmpty()) {
                sendMessage(chatId, getPlayersList(list), true);
            }
        } else {
            sendMessage(chatId, "Еще никто не зарегистрирован! undefined", true);
        }
    }

    //pidor
    public void startGame(Long chatId) {
        Optional<PotdChat> chat = chatRepository.findById(chatId);
        if (chat.isEmpty()) {
            sendMessage(chatId, "Еще никто не зарегистрирован, просьба проследовать к окошку", true);
            return;
        }
        PotdChat resChat = chat.get();
        String time = checkTodayChallengeAndGetFormatTime(resChat);
        if (time != null) {
            String str = String.format("😎 На сегодня пидор уже найден и это - %s 😎 \n Таймаут: %s", getTodayChallengeWinner(resChat), time);
            sendMessage(chatId, str, true);
            return;
        }
        //Я нашел пидора дня, но похоже, что он вышел из этого чата (вот пидор!), так что попробуйте еще раз!
        List<PotdPlayer> players = getChatPlayersList(resChat);
        if (players.isEmpty() || players.size() == 1) {
            if (players.isEmpty()) {
                sendMessage(chatId, "Поиграть не получится, никто не зарегистрован 🙁", true);
            } else {
                sendMessage(chatId, "Поиграть не получится, только один игрок зарегистрирован 🙁", true);
            }
            return;
        }
        Random random = new Random();
        int num = random.nextInt(0, players.size());

        PotdPlayer winner = players.get(num);
        long winnerId = winner.getId();

        Optional<PotdPlayer> player = playerRepository.findById(winnerId);
        if (player.isPresent()) {
            saveNewChallenge(player.get(), resChat);
            generateChallengeActivityInChat(chatId, player.get().getUser().getUserName());
            return;
        }
        sendMessage(chatId, "Что то пошло не так! 🙁", true);
    }

    private void generateChallengeActivityInChat(Long chatId, String userName) {
        String first = phraseUtils.getRandomStartLine(chatId);
        String second = phraseUtils.getRandomStartLine(chatId);
        String third = phraseUtils.getRandomLastLine(chatId);

        sendMessage(chatId, first, true);
        scheduleChatMessage(chatId, second, 3, true);
        scheduleChatMessage(chatId, third + userName, 6, false);
    }

    //Schedule Message Job

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

    //Database Methods

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
            Optional<Instant> time = TimeUtils.checkAndGetDurationToEndDay(challenge.getChallengeTime());
            if (time.isPresent()) {
                return TimeUtils.getFormattedDuration(time.get(), endDayTime());
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

    private PotdTelegramUser addOrGetUser(User user) {
        Long userId = user.id();
        Optional<PotdTelegramUser> tempUser = telegramUserRepository.findById(userId);
        if (tempUser.isEmpty()) {
            PotdTelegramUser player = new PotdTelegramUser();
            player.setId(userId);
            player.setName(user.firstName());
            player.setUserName(user.username());
            player.setRegistredAt(TimeUtils.timeStampNow());
            return telegramUserRepository.save(player);
        } else {
            return tempUser.get();
        }
    }

    private PotdChat addOrGetChat(Chat chat) {
        Long chatId = chat.id();
        Optional<PotdChat> tempChat = chatRepository.findById(chatId);
        if (tempChat.isEmpty()) {
            PotdChat chatik = new PotdChat();
            chatik.setId(chatId);
            chatik.setName(chat.title());
            chatik.setRegisteredAt(TimeUtils.timeStampNow());
            return chatRepository.save(chatik);
        } else {
            return tempChat.get();
        }
    }

    private PotdPlayer saveNewPlayer(PotdChat regChat, PotdTelegramUser regUser) {
        PotdPlayer potdPlayer = new PotdPlayer();
        potdPlayer.setChat(regChat);
        potdPlayer.setUser(regUser);
        potdPlayer.setStatus(true);
        playerRepository.save(potdPlayer);
        return potdPlayer;
    }

    private PotdChallenge saveNewChallenge(PotdPlayer player, PotdChat chat) {
        PotdChallenge challenge = new PotdChallenge();
        challenge.setPlayer(player);
        challenge.setChat(chat);
        challenge.setChallengeTime(TimeUtils.timeStampNow());
        challengeRepository.save(challenge);
        return challenge;
    }


    //Telegram methods

    /* with smiles https://github-emoji-picker.vercel.app/  https://gist.github.com/ricealexander/ae8b8cddc3939d6ba212f953701f53e6 */
    public void sendMessage(long chatId, String textToSend, boolean disableNotification) {
        SendMessage sendMessage = new SendMessage(chatId, textToSend);
        sendMessage.disableNotification(disableNotification);
        execute(sendMessage);
    }

    public <T extends BaseRequest<T, R>, R extends BaseResponse> R execute(BaseRequest<T, R> request) {
        try {
            return telegramBot.execute(request);
        } catch (Exception e) {
            log.error("Error setting bot's command list: " + e.getMessage());
            throw e;
        }
    }

    public String getBotUsername() {
        return botName;
    }
}


