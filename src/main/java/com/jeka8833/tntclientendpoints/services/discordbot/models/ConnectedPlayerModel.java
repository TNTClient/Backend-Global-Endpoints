package com.jeka8833.tntclientendpoints.services.discordbot.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@Entity
@AllArgsConstructor
@Table(name = "tnd_players")
public class ConnectedPlayerModel {
    @Id
    @Column(name = "player")
    private UUID player;

    @Column(name = "discord")
    private long discord;

    public ConnectedPlayerModel() {
    }
}
