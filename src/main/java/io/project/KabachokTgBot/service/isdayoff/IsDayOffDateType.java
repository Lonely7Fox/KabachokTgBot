package io.project.KabachokTgBot.service.isdayoff;

import io.project.KabachokTgBot.service.isdayoff.enums.DayType;

import java.util.Date;

/* copy-pasted from https://github.com/Dakla/IsDayOff */

public class IsDayOffDateType {
    /**
     * Дата
     */
    private Date date;
    /**
     * Тип дня
     * @see io.project.KabachokTgBot.service.isdayoff.enums.DayType
     */
    private DayType dayType;

    public IsDayOffDateType(Date date, DayType dayType) {
        this.date = date;
        this.dayType = dayType;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDayType(DayType dayType) {
        this.dayType = dayType;
    }

    public Date getDate() {
        return date;
    }

    public DayType getDayType() {
        return dayType;
    }
}
