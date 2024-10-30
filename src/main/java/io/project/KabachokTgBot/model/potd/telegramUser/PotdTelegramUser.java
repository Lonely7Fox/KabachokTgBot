package io.project.KabachokTgBot.model.potd.telegramUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import java.sql.Timestamp;
import java.util.Objects;

@Entity(name = "potdUser")
public class PotdTelegramUser {

    @Id
    private Long id;

    private String name;

    private String userName;

    @Column(nullable = false)
    private Timestamp registredAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Timestamp getRegistredAt() {
        return registredAt;
    }

    public void setRegistredAt(Timestamp registredAt) {
        this.registredAt = registredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PotdTelegramUser that = (PotdTelegramUser) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PotdUser{" +
                ", userId=" + id +
                ", name='" + name + '\'' +
                ", userName='" + userName + '\'' +
                ", registredAt='" + registredAt +
                '}';
    }
}
