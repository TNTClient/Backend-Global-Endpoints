package com.jeka8833.tntclientendpoints.services.discordbot.models;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Builder
@Table(name = "tnd_discord_users")
public class DiscordUser {
    @Id
    @Column("discordID")
    private final long discordID;

    @Column("roles")
    private final String roles;
}
