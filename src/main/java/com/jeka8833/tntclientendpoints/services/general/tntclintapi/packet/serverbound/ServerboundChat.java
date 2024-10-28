package com.jeka8833.tntclientendpoints.services.general.tntclintapi.packet.serverbound;

import com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.api.MojangApi;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.MinecraftServer;
import com.jeka8833.toprotocol.core.packet.PacketBase;
import com.jeka8833.toprotocol.core.serializer.PacketInputSerializer;
import com.jeka8833.toprotocol.core.serializer.PacketOutputSerializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public final class ServerboundChat implements PacketBase {
    public static final UUID EMPTY_USER = new UUID(0, 0);

    private final UUID sender;
    private final UUID receiver;
    private final MinecraftServer server;
    private final String message;

    public ServerboundChat(PacketInputSerializer serializer) {
        this.sender = serializer.readUUID();
        this.receiver = serializer.readUUID();
        this.server = MinecraftServer.fromTntApiName(serializer.readString());
        this.message = serializer.readString();
    }

    @Override
    public void write(PacketOutputSerializer serializer) {
        serializer
                .writeUUID(Objects.requireNonNullElse(sender, EMPTY_USER))
                .writeUUID(Objects.requireNonNullElse(receiver, EMPTY_USER))
                .writeString(server.getTntApiName())
                .writeString(message);
    }

    public boolean containsSender() {
        return MojangApi.isPlayerUUID(sender);
    }

    public boolean containsReceiver() {
        return MojangApi.isPlayerUUID(receiver);
    }
}
