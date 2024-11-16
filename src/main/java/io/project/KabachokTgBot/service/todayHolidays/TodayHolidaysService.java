package io.project.KabachokTgBot.service.todayHolidays;

import io.project.KabachokTgBot.httpClient.OkHttpClientImpl;
import io.project.KabachokTgBot.logback.CacheDirProperty;
import io.project.KabachokTgBot.utils.FileUtils;
import io.project.KabachokTgBot.utils.TimeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class TodayHolidaysService {

    private final Path cacheFilePath;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM").localizedBy(Locale.forLanguageTag("ru"));

    public TodayHolidaysService() {
        this.cacheFilePath = Path.of(CacheDirProperty.CACHE_DIR).resolve("holidays.txt");
    }

    public String getMessage() {
        LocalDate date = TimeUtils.todayLocalDate();
        if (checkCache()) {
            return getCachedMessage();
        } else {
            OkHttpClientImpl client = new OkHttpClientImpl();
            String holiday = client.execute("https://www.calend.ru/rss/today-holidays.rss");
            holiday = holiday.substring(holiday.indexOf("<rss")); //cut not valid part of rss

            StringBuilder builder = new StringBuilder();

            builder.append("Сегодня ").append(date.format(formatter)).append(" отмечают следующие праздники:\n").append("\n");

            Document doc = parseXMLDocument(holiday);
            NodeList list = doc.getElementsByTagName("item");
            boolean isExist = false;
            for (int i = 0; i < list.getLength(); i++) {
                String ui = list.item(i).getFirstChild().getTextContent(); //Title node
                if (ui.startsWith(String.valueOf(date.getDayOfMonth()))) {
                    builder.append(ui.substring(ui.indexOf("-") + 2)); //Just holiday name
                    builder.append("\n");
                    isExist = true;
                }
            }
            builder.delete(builder.length() - 1, builder.length());
            builder.append("\n");
            builder.append("https://www.calend.ru/day/%s/".formatted(date.format(DateTimeFormatter.ISO_LOCAL_DATE)));
            String result = builder.toString();
            if (isExist) {
                FileUtils.writeToFile(result.getBytes(), cacheFilePath);
            }
            return result;
        }
    }

    private Document parseXMLDocument(String xml) {
        Document doc;
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = builder.parse(new InputSource(new StringReader(xml)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new RuntimeException(e);
        }
        doc.getDocumentElement().normalize();
        return doc;
    }

    private boolean checkCache() {
        File cacheFile = new File(cacheFilePath.toAbsolutePath().toString());
        if (!cacheFile.exists()) {
            return false;
        } else {
            String todayDate = TimeUtils.todayLocalDate().format(formatter);
            return getCachedMessage().contains(todayDate);
        }
    }

    private String getCachedMessage() {
        byte[] bytes = FileUtils.readBytesFromFile(cacheFilePath);
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
