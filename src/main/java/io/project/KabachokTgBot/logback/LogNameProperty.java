package io.project.KabachokTgBot.logback;

import ch.qos.logback.core.PropertyDefinerBase;

public class LogNameProperty extends PropertyDefinerBase {

    public static final String FILENAME = "app.log";

    @Override
    public String getPropertyValue() {
        return FILENAME;
    }
}
