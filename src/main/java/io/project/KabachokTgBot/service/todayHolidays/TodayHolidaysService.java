package io.project.KabachokTgBot.service.todayHolidays;

import io.project.KabachokTgBot.httpClient.OkHttpClientImpl;
import io.project.KabachokTgBot.utils.TimeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TodayHolidaysService {

    private String cachedHolidays;
    private LocalDate today;

    public TodayHolidaysService() {
        today = LocalDate.MIN;
        cachedHolidays = null;
    }

    public String getMessage() {
        LocalDate date = TimeUtils.todayLocalDate();
        if (today.isEqual(date) && cachedHolidays != null) {
            return cachedHolidays;
        } else {
            OkHttpClientImpl client = new OkHttpClientImpl();
            String holiday = client.execute("https://www.calend.ru/rss/today-holidays.rss");
            holiday = holiday.substring(holiday.indexOf("<rss")); //cut not valid part of rss

            StringBuilder builder = new StringBuilder();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM").localizedBy(Locale.forLanguageTag("ru"));
            builder.append("Сегодня ").append(date.format(formatter)).append(" отмечают следующее:\n");

            Document doc = parseXMLDocument(holiday);
            NodeList list = doc.getElementsByTagName("item");
            for (int i = 0; i < list.getLength(); i++) {
                String ui = list.item(i).getFirstChild().getTextContent(); //Title node
                if (ui.startsWith(String.valueOf(date.getDayOfMonth()))) {
                    builder.append(ui.substring(ui.indexOf("-") + 2)); //Just holiday name
                    builder.append(", ");
                }
            }
            builder.delete(builder.length() - 2, builder.length());
            builder.append("\n");
            builder.append("https://www.calend.ru/day/%s/".formatted(date.format(DateTimeFormatter.ISO_LOCAL_DATE)));
            today = date;
            cachedHolidays = builder.toString();
            return cachedHolidays;
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
}
