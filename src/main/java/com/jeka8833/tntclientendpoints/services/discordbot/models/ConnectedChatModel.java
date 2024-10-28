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
@Table(name = "tnd_live_chat_list")
public class ConnectedChatModel {
    @Id
    @Column(name = "chatid")
    private long chatID;

    @Column(name = "userid")
    private long userID;
}
