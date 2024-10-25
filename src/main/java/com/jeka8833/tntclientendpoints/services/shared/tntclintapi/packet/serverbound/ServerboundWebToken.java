package com.jeka8833.tntclientendpoints.services.shared.tntclintapi.packet.serverbound;

import com.jeka8833.toprotocol.core.packet.PacketBase;
import com.jeka8833.toprotocol.core.serializer.PacketInputSerializer;
import com.jeka8833.toprotocol.core.serializer.PacketOutputSerializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public final class ServerboundWebToken implements PacketBase {
    private static final UUID EMPTY_UUID = new UUID(0, 0);

    private final UUID user;
    private final UUID token;

    public ServerboundWebToken(UUID user) {
        this.user = user;
        this.token = EMPTY_UUID;
    }

    public ServerboundWebToken(PacketInputSerializer serializer) {
        this.user = serializer.readUUID();
        this.token = serializer.readUUID();
    }

    @Override
    public void write(PacketOutputSerializer serializer) {
        serializer
                .writeUUID(Objects.requireNonNullElse(user, EMPTY_UUID))
                .writeUUID(Objects.requireNonNullElse(token, EMPTY_UUID));
    }

    public boolean isLogOutOperation() {
        return token == null || token.equals(EMPTY_UUID);
    }
}
