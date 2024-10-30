package io.project.KabachokTgBot.utils.phrase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.springframework.core.io.ClassPathResource;

public class PhraseUtils {

    private final List<String> startLines;
    private final List<String> lastLines;
    private final LastUsedPhraseCache lastUsedPhraseCache;

    public PhraseUtils() {
//        File startFile = new File("phrases/StartPhrase.txt");
//        File lastFile = new File("phrases/LastPhrase.txt");
        try {
            this.startLines = resolveFile(new ClassPathResource("phrases/StartPhrase.txt").getInputStream());
            this.lastLines = resolveFile(new ClassPathResource("phrases/LastPhrase.txt").getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.lastUsedPhraseCache = new LastUsedPhraseCache();
    }

    private int getRandomInt(int bound) {
        return new Random().nextInt(0, bound);
    }

    private boolean validate(Long chatId, Integer phraseId) {
        Integer number = lastUsedPhraseCache.getFirstPhraseId(chatId);
        return number == null || (number != null && Objects.equals(number, phraseId));
    }

    public String getRandomStartLine(Long chatId) {
        int phraseId = getRandomInt(startLines.size());
        return validate(chatId, phraseId) ?
                startLines.get(phraseId) : getRandomStartLine(chatId);
    }

    public String getRandomLastLine(Long chatId) {
        int phraseId = getRandomInt(lastLines.size());
        return validate(chatId, phraseId) ?
                lastLines.get(phraseId) : getRandomLastLine(chatId);
    }

    private List<String> resolveFile(InputStream stream) {
        try {
            List<String> lines = new ArrayList<>();

            InputStreamReader inputStreamReader = new InputStreamReader(stream);
            BufferedReader reader = new BufferedReader(inputStreamReader);

            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
            return lines;
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }
}
