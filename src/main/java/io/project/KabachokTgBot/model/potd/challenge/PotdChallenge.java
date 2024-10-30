package io.project.KabachokTgBot.model.potd.challenge;

import io.project.KabachokTgBot.model.potd.chat.PotdChat;
import io.project.KabachokTgBot.model.potd.player.PotdPlayer;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.sql.Timestamp;
import java.util.Objects;

@Entity(name = "potdChallenge")
public class PotdChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "player_id", referencedColumnName = "id")
    private PotdPlayer player;

    @ManyToOne
    @JoinColumn(name = "chat_id", referencedColumnName = "id")
    private PotdChat chat;

    private Timestamp challengeTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PotdPlayer getPlayer() {
        return player;
    }

    public void setPlayer(PotdPlayer player) {
        this.player = player;
    }

    public PotdChat getChat() {
        return chat;
    }

    public void setChat(PotdChat chat) {
        this.chat = chat;
    }

    public Timestamp getChallengeTime() {
        return challengeTime;
    }

    public void setChallengeTime(Timestamp challengeTime) {
        this.challengeTime = challengeTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PotdChallenge that = (PotdChallenge) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PotdChallenge{" +
                "id=" + id +
                ", player=" + player +
                ", chat=" + chat +
                ", challengeTime=" + challengeTime +
                '}';
    }
}
