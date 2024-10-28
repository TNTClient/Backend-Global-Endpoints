package com.jeka8833.tntclientendpoints.services.discordbot;

import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

public class DeferReplyWrapper extends ReplyWrapper {
    public DeferReplyWrapper(IReplyCallback event) {
        super(event);

        event.deferReply(true).queue();
    }
}
