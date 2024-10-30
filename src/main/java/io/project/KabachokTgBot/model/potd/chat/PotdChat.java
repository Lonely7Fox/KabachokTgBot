package io.project.KabachokTgBot.model.potd.chat;

import io.project.KabachokTgBot.model.potd.player.PotdPlayer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Objects;

@Entity(name = "potdChat")
public class PotdChat {

    @Id
    private Long id;

    private String name;

    @Column(nullable = false)
    private Timestamp registeredAt;

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

    public Timestamp getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Timestamp registeredAt) {
        this.registeredAt = registeredAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PotdChat potdChat = (PotdChat) o;
        return Objects.equals(id, potdChat.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PotdChat{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", registeredAt=" + registeredAt +
                '}';
    }

    @OneToMany(mappedBy = "chat")
    private Collection<PotdPlayer> potdPlayer;

    public Collection<PotdPlayer> getPotdPlayer() {
        return potdPlayer;
    }

    public void setPotdPlayer(Collection<PotdPlayer> potdPlayer) {
        this.potdPlayer = potdPlayer;
    }
}