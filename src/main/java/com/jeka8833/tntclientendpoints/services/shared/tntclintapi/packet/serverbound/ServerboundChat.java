package com.jeka8833.tntclientendpoints.services.shared.tntclintapi.packet.serverbound;

import com.jeka8833.tntclientendpoints.services.discordbot.service.mojang.api.MojangAPI;
import com.jeka8833.tntclientendpoints.services.shared.tntclintapi.MinecraftServer;
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
    private static final UUID EMPTY_UUID = new UUID(0, 0);

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

    public ServerboundChat(UUID receiver, String message) {
        this(EMPTY_UUID, receiver, MinecraftServer.GLOBAL, message);
    }

    public ServerboundChat(UUID sender, UUID receiver, String message) {
        this(sender, receiver, MinecraftServer.GLOBAL, message);
    }

    public ServerboundChat(UUID sender, MinecraftServer server, String message) {
        this(sender, EMPTY_UUID, server, message);
    }

    @Override
    public void write(PacketOutputSerializer serializer) {
        serializer
                .writeUUID(Objects.requireNonNullElse(sender, EMPTY_UUID))
                .writeUUID(Objects.requireNonNullElse(receiver, EMPTY_UUID))
                .writeString(server.getTntApiName())
                .writeString(message);
    }

    public boolean containsSender() {
        return MojangAPI.isPlayerUUID(sender);
    }

    public boolean containsReceiver() {
        return MojangAPI.isPlayerUUID(receiver);
    }
}
