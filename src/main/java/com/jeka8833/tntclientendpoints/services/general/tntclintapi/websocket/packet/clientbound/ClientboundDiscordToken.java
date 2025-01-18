package com.jeka8833.tntclientendpoints.services.general.tntclintapi.websocket.packet.clientbound;

import com.jeka8833.tntclientendpoints.services.general.tntclintapi.websocket.packet.ClientBoundPacket;
import com.jeka8833.toprotocol.core.serializer.InputByteArray;
import com.jeka8833.toprotocol.core.serializer.OutputByteArray;
import com.jeka8833.toprotocol.extension.serializer.SerializeUUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
@ExtensionMethod(value = {SerializeUUID.class})
public final class ClientboundDiscordToken implements ClientBoundPacket {
    private final UUID player;
    private final int code;

    public ClientboundDiscordToken(InputByteArray serializer) {
        this.player = serializer.readUUID();
        this.code = serializer.readInt();
    }

    @Override
    public void write(@NotNull OutputByteArray serializer, int protocolVersion) {
        serializer.writeUUID(player);
        serializer.writeInt(code);
    }
}
