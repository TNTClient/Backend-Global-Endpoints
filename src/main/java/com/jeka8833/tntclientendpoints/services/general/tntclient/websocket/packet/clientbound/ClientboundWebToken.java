package com.jeka8833.tntclientendpoints.services.general.tntclient.websocket.packet.clientbound;

import com.jeka8833.tntclientendpoints.services.general.tntclient.websocket.packet.ClientBoundPacket;
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
public final class ClientboundWebToken implements ClientBoundPacket {
    private final UUID player;
    private final boolean register;

    public ClientboundWebToken(InputByteArray serializer) {
        this.player = serializer.readUUID();
        this.register = serializer.readBoolean();
    }

    @Override
    public void write(@NotNull OutputByteArray serializer, int protocolVersion) {
        serializer.writeUUID(player);
        serializer.writeBoolean(register);
    }
}
