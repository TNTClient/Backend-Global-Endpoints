package com.jeka8833.tntclientendpoints.services.general.tntclient.websocket.packet;

import com.jeka8833.toprotocol.core.packet.PacketBase;
import com.jeka8833.toprotocol.core.serializer.OutputByteArray;
import org.jetbrains.annotations.NotNull;

public interface ClientBoundPacket extends PacketBase {
    default void write(@NotNull OutputByteArray serializer, int protocolVersion) {
    }
}
