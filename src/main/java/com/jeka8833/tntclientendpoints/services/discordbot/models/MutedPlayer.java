package com.jeka8833.tntclientendpoints.services.discordbot.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "tnd_muted_users")
public class MutedPlayer {
    @Id
    @Column(name = "player")
    private UUID player;

    @Column(name = "moderator")
    private long moderator;

    @Column(name = "reason")
    private String reason;

    @Column(name = "unmute_time")
    private ZonedDateTime unmuteTime;

    public MutedPlayer() {
    }
}
