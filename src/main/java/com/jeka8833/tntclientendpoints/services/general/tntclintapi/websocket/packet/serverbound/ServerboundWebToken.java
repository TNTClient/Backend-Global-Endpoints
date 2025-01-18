package com.jeka8833.tntclientendpoints.services.general.tntclintapi.websocket.packet.serverbound;

import com.jeka8833.tntclientendpoints.services.general.tntclintapi.websocket.packet.ServerBoundPacket;
import com.jeka8833.toprotocol.core.serializer.InputByteArray;
import com.jeka8833.toprotocol.core.serializer.OutputByteArray;
import com.jeka8833.toprotocol.extension.serializer.SerializeUUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
@ExtensionMethod(value = {SerializeUUID.class})
public final class ServerboundWebToken implements ServerBoundPacket {
    private static final UUID EMPTY_UUID = new UUID(0, 0);

    private final UUID user;
    private final UUID token;

    public ServerboundWebToken(UUID user) {
        this.user = user;
        this.token = EMPTY_UUID;
    }

    public ServerboundWebToken(InputByteArray serializer, int protocolVersion) {
        this.user = serializer.readUUID();
        this.token = serializer.readUUID();
    }

    @Override
    public void write(@NotNull OutputByteArray serializer) {
        serializer.writeUUID(Objects.requireNonNullElse(user, EMPTY_UUID));
        serializer.writeUUID(Objects.requireNonNullElse(token, EMPTY_UUID));
    }

    public boolean isLogOutOperation() {
        return token == null || token.equals(EMPTY_UUID);
    }
}
