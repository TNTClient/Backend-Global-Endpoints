package com.jeka8833.tntclientendpoints.services.discordbot.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
@Table(name = "discordbot_live_chat_connection")
public class ConnectedChatModel {
    @Id
    @Column(name = "chat_id")
    private long chatID;

    @Column(name = "user_id")
    private long userID;
}
