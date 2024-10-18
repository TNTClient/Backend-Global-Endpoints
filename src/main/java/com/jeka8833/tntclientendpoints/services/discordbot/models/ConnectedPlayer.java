package com.jeka8833.tntclientendpoints.services.discordbot.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@Entity
@Table(name = "tnd_players")
public class ConnectedPlayer {
    @Id
    @Column(name = "player")
    private UUID player;

    @Column(name = "discord")
    private long discord;

    public ConnectedPlayer() {
    }
}
