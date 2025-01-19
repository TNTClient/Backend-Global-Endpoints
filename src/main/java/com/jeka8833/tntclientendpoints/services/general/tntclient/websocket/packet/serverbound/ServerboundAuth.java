package com.jeka8833.tntclientendpoints.services.general.tntclient.websocket.packet.serverbound;

import com.jeka8833.tntclientendpoints.services.general.tntclient.websocket.packet.ServerBoundPacket;
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
public final class ServerboundAuth implements ServerBoundPacket {
    private final UUID user;
    private final UUID password;

    public ServerboundAuth(InputByteArray serializer, int protocolVersion) {
        this(serializer.readUUID(), serializer.readUUID());
    }

    @Override
    public void write(@NotNull OutputByteArray serializer) {
        serializer.writeUUID(user);
        serializer.writeUUID(password);
    }
}
