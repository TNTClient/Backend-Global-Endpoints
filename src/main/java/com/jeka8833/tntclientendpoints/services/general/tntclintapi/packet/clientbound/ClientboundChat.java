package com.jeka8833.tntclientendpoints.services.general.tntclintapi.packet.clientbound;

import com.jeka8833.tntclientendpoints.services.general.tntclintapi.MinecraftServer;
import com.jeka8833.toprotocol.core.packet.PacketBase;
import com.jeka8833.toprotocol.core.serializer.PacketInputSerializer;
import com.jeka8833.toprotocol.core.serializer.PacketOutputSerializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public final class ClientboundChat implements PacketBase {
    private final @Nullable UUID sender;
    private final @Nullable UUID receiver;
    private final @NotNull MinecraftServer server;
    private final @NotNull String message;

    public ClientboundChat(PacketInputSerializer serializer) {
        this.sender = serializer.readOptionally(PacketInputSerializer::readUUID);
        this.receiver = serializer.readOptionally(PacketInputSerializer::readUUID);
        this.server = MinecraftServer.fromTntApiName(serializer.readString());
        this.message = serializer.readString();
    }

    @Override
    public void write(PacketOutputSerializer serializer) {
        serializer
                .writeOptionally(sender, PacketOutputSerializer::writeUUID)
                .writeOptionally(receiver, PacketOutputSerializer::writeUUID)
                .writeString(server.getTntApiName())
                .writeString(message);
    }
}
