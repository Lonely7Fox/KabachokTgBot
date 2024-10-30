package io.project.KabachokTgBot.logback;

import ch.qos.logback.core.PropertyDefinerBase;
import io.project.KabachokTgBot.utils.OSUtils;

public class LogDirProperty extends PropertyDefinerBase {

    //public static final String LOG_DIR = (System.getProperty("log_dir") == null) ? "logs" : System.getProperty("log_dir");
    public static final String LOG_DIR = (OSUtils.IS_OS_WINDOWS) ? "logs" : "/var/log/KabachokTgBot";

    @Override
    public String getPropertyValue() {
        return LOG_DIR;
    }
}
