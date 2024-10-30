package io.project.KabachokTgBot.model.potd.player;

import io.project.KabachokTgBot.model.potd.telegramUser.PotdTelegramUser;
import io.project.KabachokTgBot.model.potd.chat.PotdChat;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.util.Objects;

@Entity(name = "potdPlayer")
public class PotdPlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chat_id", referencedColumnName = "id")
    private PotdChat chat;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private PotdTelegramUser user;

    private Boolean status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PotdChat getChat() {
        return chat;
    }

    public void setChat(PotdChat chat) {
        this.chat = chat;
    }

    public PotdTelegramUser getUser() {
        return user;
    }

    public void setUser(PotdTelegramUser user) {
        this.user = user;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PotdPlayer player = (PotdPlayer) o;
        return Objects.equals(id, player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PotdPlayer{" +
                "id=" + id +
                ", chat=" + chat +
                ", user=" + user +
                ", status=" + status +
                '}';
    }
}
