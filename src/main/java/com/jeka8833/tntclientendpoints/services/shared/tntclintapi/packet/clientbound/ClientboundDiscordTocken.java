package com.jeka8833.tntclientendpoints.services.shared.tntclintapi.packet.clientbound;

import com.jeka8833.toprotocol.core.packet.PacketBase;
import com.jeka8833.toprotocol.core.serializer.PacketInputSerializer;
import com.jeka8833.toprotocol.core.serializer.PacketOutputSerializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public final class ClientboundDiscordTocken implements PacketBase {
    private final UUID player;
    private final int code;

    public ClientboundDiscordTocken(PacketInputSerializer serializer) {
        this.player = serializer.readUUID();
        this.code = serializer.readInt();
    }

    @Override
    public void write(PacketOutputSerializer serializer) {
        serializer.writeUUID(player).writeInt(code);
    }
}
