package io.project.KabachokTgBot.logback;

import ch.qos.logback.core.PropertyDefinerBase;
import io.project.KabachokTgBot.utils.OSUtils;

public class CacheDirProperty extends PropertyDefinerBase {

    public static final String CACHE_DIR = (OSUtils.IS_OS_WINDOWS) ? "cache" : "/var/log/KabachokTgBotCache";

    @Override
    public String getPropertyValue() {
        return CACHE_DIR;
    }
}
