package io.project.KabachokTgBot.service.todayHolidays;

public enum HolidayType {
    INTERNATIONAL("Международные праздники",5),
    RUSSIAN("Праздники России", 4),
    PROFESSIONAL("Профессиональные праздники", 3),
    UNITED_NATIONS("Праздники ООН", 2),
    OTHER("OTHER", 1);

    private final String type;
    private final int value;

    HolidayType(String type, int value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public int getValue() { return value;}

    public static HolidayType of(String value) throws IllegalArgumentException {
        for (HolidayType browserType : HolidayType.values()) {
            if (browserType.type.equals(value)){
                return browserType;
            }
        }
        return OTHER;
    }
}
