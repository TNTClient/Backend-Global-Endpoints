package com.jeka8833.tntclientendpoints.services.shared.tntclintapi.packet.clientbound;

import com.jeka8833.toprotocol.core.packet.PacketBase;
import com.jeka8833.toprotocol.core.serializer.PacketInputSerializer;
import com.jeka8833.toprotocol.core.serializer.PacketOutputSerializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public final class ClientboundWebToken implements PacketBase {
    private final UUID player;
    private final boolean register;

    public ClientboundWebToken(PacketInputSerializer serializer) {
        this.player = serializer.readUUID();
        this.register = serializer.readBoolean();
    }

    @Override
    public void write(PacketOutputSerializer serializer) {
        serializer.writeUUID(player).writeBoolean(register);
    }
}
