package com.jeka8833.tntclientendpoints.services.shared.tntclintapi.packet.serverbound;

import com.jeka8833.toprotocol.core.packet.PacketBase;
import com.jeka8833.toprotocol.core.serializer.PacketInputSerializer;
import com.jeka8833.toprotocol.core.serializer.PacketOutputSerializer;

import java.util.UUID;

public final class ServerboundAuth implements PacketBase {
    private final UUID user;
    private final UUID password;

    public ServerboundAuth(UUID user, UUID password) {
        this.user = user;
        this.password = password;
    }

    public ServerboundAuth(PacketInputSerializer serializer) {
        this(serializer.readUUID(), serializer.readUUID());
    }

    @Override
    public void write(PacketOutputSerializer serializer) {
        serializer.writeUUID(user).writeUUID(password);
    }

    public UUID getUser() {
        return user;
    }

    public UUID getPassword() {
        return password;
    }
}
