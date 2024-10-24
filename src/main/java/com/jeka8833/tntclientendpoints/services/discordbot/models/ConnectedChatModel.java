package com.jeka8833.tntclientendpoints.services.discordbot.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor

@Entity
@Table(name = "tnd_live_chat_list")
public class ConnectedChatModel {
    @Id
    @Column(name = "chatID")
    private long chatID;

    @Column(name = "userID")
    private long userID;
}
