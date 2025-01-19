package com.jeka8833.tntclientendpoints.services.general.tntclient.websocket.packet.serverbound;

import com.jeka8833.tntclientendpoints.services.general.tntclient.websocket.MinecraftServer;
import com.jeka8833.tntclientendpoints.services.general.tntclient.websocket.packet.ServerBoundPacket;
import com.jeka8833.toprotocol.core.serializer.InputByteArray;
import com.jeka8833.toprotocol.core.serializer.OutputByteArray;
import com.jeka8833.toprotocol.extension.serializer.SerializeOptional;
import com.jeka8833.toprotocol.extension.serializer.SerializeString;
import com.jeka8833.toprotocol.extension.serializer.SerializeUUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
@ExtensionMethod(value = {SerializeUUID.class, SerializeOptional.class, SerializeString.class})
public final class ServerboundChat implements ServerBoundPacket {
    private final @Nullable UUID sender;
    private final @Nullable UUID receiver;
    private final @NotNull MinecraftServer server;
    private final @NotNull String message;
    private final boolean isSystemMessage;

    public ServerboundChat(InputByteArray serializer, int protocolVersion) {
        this.sender = serializer.readOptionally(SerializeUUID::readUUID);
        this.receiver = serializer.readOptionally(SerializeUUID::readUUID);
        this.server = MinecraftServer.fromTntApiName(serializer.readUTF8());
        this.message = serializer.readUTF8();
        this.isSystemMessage = serializer.readBoolean();
    }

    @Override
    public void write(@NotNull OutputByteArray serializer) {
        serializer.writeOptionally(sender, SerializeUUID::writeUUID);
        serializer.writeOptionally(receiver, SerializeUUID::writeUUID);
        serializer.writeUTF8(server.getTntApiName());
        serializer.writeUTF8(message);
        serializer.writeBoolean(isSystemMessage);
    }
}
