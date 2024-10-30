package io.project.KabachokTgBot.utils;

import static java.util.Locale.ROOT;

public class OSUtils {

    public OSUtils() {
    }

    public static final String OS_NAME = getSystemProperty("os.name");

    public static final boolean IS_OS_WINDOWS = isOSNameContains("windows");
    public static final boolean IS_OS_LINUX = isOSNameContains("linux");

    private static boolean isOSNameContains(final String osNamePart) {
        if (OSUtils.OS_NAME == null) {
            return false;
        }
        return OSUtils.OS_NAME.toLowerCase(ROOT).contains(osNamePart);
    }

    private static String getSystemProperty(final String property) {
        try {
            return System.getProperty(property);
        } catch (final SecurityException ex) {
            // we are not allowed to look at this property
            // System.err.println("Caught a SecurityException reading the system property '" + property
            // + "'; the SystemUtils property value will default to null.");
            return null;
        }
    }
}
