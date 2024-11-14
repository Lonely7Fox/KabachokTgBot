package io.project.KabachokTgBot.service.isdayoff.enums;

/* copy-pasted from https://github.com/Dakla/IsDayOff */

public enum DirectionType {
    PAST(0),
    FUTURE(1);

    private int id;

    DirectionType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
