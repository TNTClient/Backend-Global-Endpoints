package com.jeka8833.tntclientendpoints.services.discordbot.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(name = "tnd_live_chat_list")
public class ConnectedChat {
    @Id
    @Column(name = "chatID")
    private long chatID;

    @Column(name = "userID")
    private long userID;

    public ConnectedChat() {
    }
}
