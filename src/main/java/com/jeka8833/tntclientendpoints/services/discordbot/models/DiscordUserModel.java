package com.jeka8833.tntclientendpoints.services.discordbot.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter

@Entity
@Table(name = "discord_user")
public class DiscordUserModel {
    @Id
    @Column(name = "user")
    private long discordID;

    @Column(name = "roles")
    private String roles;
}
