package com.jeka8833.tntclientwebendpoints.models;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Builder
@Table(name = "tnd_live_chat_list")
public class ConnectedChat {
    @Id
    @Column("chatID")
    private final long chatID;

    @Column("userID")
    private final long userID;
}
