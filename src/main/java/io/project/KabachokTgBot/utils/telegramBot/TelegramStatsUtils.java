package io.project.KabachokTgBot.utils.telegramBot;

import io.project.KabachokTgBot.model.potd.challenge.PotdChallenge;
import io.project.KabachokTgBot.model.potd.player.PotdPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelegramStatsUtils {

    public static String getStatsMessage(List<PotdChallenge> challengeList, String startMsg) {
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
            String text = String.format("%d. %s - %d раз(а)\n", index, entry.getKey().getUser().getUserName(), entry.getValue());
            stringBuilder.append(text);
            index++;
        }
        stringBuilder.append("\n");
        stringBuilder.append("Всего участников — ");
        stringBuilder.append(entries.size());
        return stringBuilder.toString();
    }

    public static String getPlayersList(List<PotdPlayer> list) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Участники из чата: ").append("\n");
        for (PotdPlayer p : list) {
            stringBuilder.append(p.getUser().getUserName()).append(", ");
        }
        String str = stringBuilder.toString();
        str = str.substring(0, str.length() - 2);
        return str;
    }
}
