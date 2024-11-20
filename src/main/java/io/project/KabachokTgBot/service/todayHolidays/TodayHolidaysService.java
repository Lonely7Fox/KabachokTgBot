package io.project.KabachokTgBot.service.todayHolidays;

import io.project.KabachokTgBot.httpClient.OkHttpClientImpl;
import io.project.KabachokTgBot.utils.TimeUtils;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
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
import java.util.ArrayList;
import java.util.List;

import static io.project.KabachokTgBot.service.todayHolidays.TodayHolidaysServiceCacheUtils.checkCache;
import static io.project.KabachokTgBot.service.todayHolidays.TodayHolidaysServiceCacheUtils.getCachedMessage;
import static io.project.KabachokTgBot.service.todayHolidays.TodayHolidaysServiceCacheUtils.setCachedMessage;
import static io.project.KabachokTgBot.utils.TimeUtils.ddMMMMRuPattern;

public class TodayHolidaysService {

    public TodayHolidaysService() {

    }

    private record Holiday(String type, String value) {}

    public String getMessage() {
        LocalDate date = TimeUtils.todayLocalDate();
        if (checkCache()) {
            return getCachedMessage();
        }
        String dayNumber = String.valueOf(date.getDayOfMonth());
        String response = executeRequest();
        Document doc = parseXMLDocument(response);
        NodeList itemNodes = doc.getElementsByTagName("item");

        List<Holiday> listHolidays = new ArrayList<>();
        for (int i = 0; i < itemNodes.getLength(); i++) {
            NodeList itemChildNodes = itemNodes.item(i).getChildNodes();
            Holiday holiday = parseItemInnerNodes(itemChildNodes, dayNumber);
            if (holiday != null) {
                listHolidays.add(holiday);
            }
        }
        String result = resultMessageBuilder(listHolidays, date);
        if (!listHolidays.isEmpty()) {
            setCachedMessage(result);
        }
        return result;
    }

    //todo RSS contains just a part of holidays, 80% of them
    private String executeRequest() {
        OkHttpClientImpl client = new OkHttpClientImpl();
        String holiday = client.execute("https://www.calend.ru/rss/today-holidays.rss");
        return holiday.substring(holiday.indexOf("<rss")); //cut not valid part of rss
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

    @Nullable
    private Holiday parseItemInnerNodes(NodeList itemChildNodes, String dayNumber) {
        String type = null;
        String value = null;
        for (int j = 0; j < itemChildNodes.getLength(); j++) {
            Node node = itemChildNodes.item(j);
            //<title>21 ноября 2024 - Всемирный день приветствий</title>
            if (node.getNodeName().equals("title")) { //title with date
                String titleText = node.getTextContent();
                if (titleText.startsWith(dayNumber)) { //check today
                    value = titleText.substring(titleText.indexOf("-") + 2); //Just holiday name
                } else {
                    break; //check next node
                }
            }
            if (node.getNodeName().equals("category")) {
                type = node.getTextContent();
            }
            continue; //check next innerNode
        }
        if (value != null && type != null) {
            return new Holiday(type, value);
        } else {
            return null;
        }
    }

    private String resultMessageBuilder(List<Holiday> listHolidays, LocalDate date) {
        StringBuilder builder = new StringBuilder();
        builder.append("Праздничные события ").append(date.format(ddMMMMRuPattern)).append(" :\n").append("\n");
        if (!listHolidays.isEmpty()) {
            listHolidays.sort((o1, o2) -> HolidayType.of(o2.type()).getValue() - HolidayType.of(o1.type()).getValue());
            listHolidays.forEach(holiday -> {
                if (HolidayType.of(holiday.type()) != HolidayType.OTHER) {
                    builder.append(holiday.value()).append("\n");
                }
            });
            builder.delete(builder.length() - 1, builder.length());
        }
        builder.append("\n");
        builder.append("https://www.calend.ru/day/%s/".formatted(date.format(DateTimeFormatter.ISO_LOCAL_DATE)));
        return builder.toString();
    }
}
