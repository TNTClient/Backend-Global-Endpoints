package com.jeka8833.tntclientendpoints.services.general.tntclintapi.packet.serverbound;

import com.jeka8833.toprotocol.core.packet.PacketBase;
import com.jeka8833.toprotocol.core.serializer.PacketInputSerializer;
import com.jeka8833.toprotocol.core.serializer.PacketOutputSerializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public final class ServerboundAuth implements PacketBase {
    private final UUID user;
    private final UUID password;

    public ServerboundAuth(PacketInputSerializer serializer) {
        this(serializer.readUUID(), serializer.readUUID());
    }

    @Override
    public void write(PacketOutputSerializer serializer) {
        serializer.writeUUID(user).writeUUID(password);
    }
}
