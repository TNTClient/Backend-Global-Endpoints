package com.jeka8833.tntclientendpoints.services.shared.tntclintapi.packet.serverbound;

import com.jeka8833.toprotocol.core.packet.PacketBase;
import com.jeka8833.toprotocol.core.serializer.PacketInputSerializer;
import com.jeka8833.toprotocol.core.serializer.PacketOutputSerializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public final class ServerboundDiscordToken implements PacketBase {
    public static final int STATUS_LINKED_SUCCESS = 0;
    public static final int STATUS_LINKED_FAILED = 1;
    public static final int STATUS_INTERNAL_ERROR = 2;

    private final UUID player;
    private final int status;

    public ServerboundDiscordToken(PacketInputSerializer serializer) {
        this.player = serializer.readUUID();
        this.status = serializer.readInt();
    }

    @Override
    public void write(PacketOutputSerializer serializer) {
        serializer.writeUUID(player).writeInt(status);
    }
}
