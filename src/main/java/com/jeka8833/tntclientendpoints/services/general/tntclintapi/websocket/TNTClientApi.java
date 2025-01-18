package com.jeka8833.tntclientendpoints.services.general.tntclintapi.websocket;

import com.jeka8833.tntclientendpoints.services.general.tntclintapi.websocket.packet.ClientBoundPacket;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.websocket.packet.ServerBoundPacket;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.websocket.packet.clientbound.ClientboundAuth;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.websocket.packet.clientbound.ClientboundChat;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.websocket.packet.clientbound.ClientboundDiscordToken;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.websocket.packet.clientbound.ClientboundWebToken;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.websocket.packet.serverbound.ServerboundAuth;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.websocket.packet.serverbound.ServerboundChat;
import com.jeka8833.tntclientendpoints.services.general.tntclintapi.websocket.packet.serverbound.ServerboundWebToken;
import com.jeka8833.toprotocol.core.register.ClientBoundRegistry;
import com.jeka8833.toprotocol.core.register.PacketRegistryBuilder;
import com.jeka8833.toprotocol.core.serializer.InputByteArray;
import com.jeka8833.toprotocol.core.serializer.OutputByteArray;
import lombok.Locked;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Slf4j
public class TNTClientApi {
    private static final int MAX_PACKET_SIZE = 2 * 1024;

    private static final ClientBoundRegistry<Byte, ClientBoundPacket, ServerBoundPacket, Integer> PACKET_REGISTRY =
            new PacketRegistryBuilder<Byte, ClientBoundPacket, ServerBoundPacket, Integer>()
                    .register((byte) 248, build -> build
                            .clientbound(ClientboundChat.class, ClientboundChat::new)
                            .serverbound(ServerboundChat.class, ServerboundChat::new)
                    )
                    .register((byte) 253, build -> build
                            .clientbound(ClientboundWebToken.class, (input, _) ->
                                    new ClientboundWebToken(input))
                            .serverbound(ServerboundWebToken.class, (input, protocolVersion) ->
                                    new ServerboundWebToken(input, protocolVersion))
                    )
                    .register((byte) 254, build -> build
                            .clientbound(ClientboundDiscordToken.class, (inputByteArray, _) ->
                                    new ClientboundDiscordToken(inputByteArray))
                    )
                    .register((byte) 255, build -> build
                            .clientbound(ClientboundAuth.class, ClientboundAuth::new)
                            .serverbound(ServerboundAuth.class, ServerboundAuth::new)
                    )
                    .buildForClient();

    private final Map<Class<? extends ClientBoundPacket>, Collection<Consumer<ClientBoundPacket>>> listenersMap =
            new ConcurrentHashMap<>();

    private final Request request;
    private final OkHttpClient client;
    private final UUID user;
    private final UUID password;
    private final long reconnectDelayNanos;
    private final ScheduledExecutorService executorService;

    private long lastConnectTime = 0;
    private @Nullable Future<?> authorisingFuture;
    private @Nullable Future<?> reconnectingFuture;
    private @Nullable WebSocket webSocket;
    private State state = State.CLOSED;

    private final WebSocketListener webSocketListener = new WebSocketListener() {
        @Override
        public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
            super.onOpen(webSocket, response);

            if (state != State.CLOSED) state = State.AUTHORISING;

            if (authorisingFuture != null) authorisingFuture.cancel(true);

            authorisingFuture = executorService.schedule(() -> {
                if (state == State.AUTHORISING) doReconnect();
            }, reconnectDelayNanos, TimeUnit.NANOSECONDS);

            send(new ServerboundAuth(user, password));
        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
            super.onMessage(webSocket, bytes);

            InputByteArray serializer = new InputByteArray(bytes.toByteArray());
            try {
                byte packetId = serializer.readByte();

                ClientBoundPacket packet = PACKET_REGISTRY.createClientBoundPacket(packetId, serializer);
                if (packet == null) {
                    log.warn("Unknown packet id: {}", packetId & 0xFF);
                    return;
                }

                Collection<Consumer<ClientBoundPacket>> listeners = listenersMap.get(packet.getClass());
                if (listeners != null) {
                    for (Consumer<ClientBoundPacket> listener : listeners) {
                        try {
                            listener.accept(packet);
                        } catch (Exception e) {
                            log.warn("Exception in listener", e);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Fail to parse packet", e);
            }
        }

        @Override
        public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
            super.onFailure(webSocket, t, response);

            if (state != State.CLOSED) state = State.RECONNECTING;

            log.error("Exception in WebSocket", t);

            doReconnect();
        }

        @Override
        public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
            super.onClosed(webSocket, code, reason);

            if (state != State.CLOSED) state = State.RECONNECTING;

            log.info("Server close WebSocket with code {}: {}", code, reason);

            doReconnect();
        }
    };

    public TNTClientApi(Request request, OkHttpClient client, UUID user, UUID password, long reconnectDelayNanos,
                        ScheduledExecutorService executorService) {
        this.request = request;
        this.client = client;
        this.user = user;
        this.password = password;
        this.reconnectDelayNanos = reconnectDelayNanos;
        this.executorService = executorService;

        registerListener(ClientboundAuth.class, _ -> {
            if (authorisingFuture != null) authorisingFuture.cancel(true);

            if (state != State.CLOSED) state = State.CONNECTED;

            log.info("TNTClient API Connected!");
        });
    }

    @Locked
    public void connect() {
        if (state != State.CLOSED) return;

        state = State.CONNECTING;
        doReconnect();
    }

    @Locked
    public void disconnect() {
        if (authorisingFuture != null) {
            authorisingFuture.cancel(true);

            authorisingFuture = null;
        }

        if (reconnectingFuture != null) {
            reconnectingFuture.cancel(true);

            reconnectingFuture = null;
        }

        if (webSocket != null) {
            webSocket.close(1000, null);

            webSocket = null;
        }

        state = State.CLOSED;
    }

    public <T extends ClientBoundPacket> void registerListener(@NotNull Class<T> packetClass,
                                                               @NotNull Consumer<T> listener) {
        Collection<Consumer<ClientBoundPacket>> list = listenersMap.computeIfAbsent(packetClass,
                _ -> new CopyOnWriteArrayList<>());

        //noinspection unchecked
        list.add((Consumer<ClientBoundPacket>) listener);
    }

    public void send(@NotNull ServerBoundPacket packet, long maxTimeSend, TimeUnit unit) {
        if (!send(packet)) {
            long endTime = System.nanoTime() + unit.toNanos(maxTimeSend);

            executorService.scheduleWithFixedDelay(() -> {
                if (System.nanoTime() - endTime > 0) {
                    throw new CancellationException("Packet is not sent");
                }

                if (send(packet)) {
                    throw new CancellationException("Packet is sent");
                }
            }, 500, 500, TimeUnit.MILLISECONDS);
        }
    }

    public boolean send(@NotNull ServerBoundPacket packet) throws RuntimeException {
        Byte identifier = PACKET_REGISTRY.getServerBoundPacketKey(packet.getClass());
        if (identifier == null) {
            throw new UnsupportedOperationException("Packet not registered: " + packet.getClass().getName());
        }

        OutputByteArray stream = new OutputByteArray(MAX_PACKET_SIZE);
        stream.writeByte(identifier);
        packet.write(stream);

        WebSocket webSocket = this.webSocket;
        if (webSocket != null &&
                (state == State.CONNECTED || state == State.AUTHORISING && packet instanceof ServerboundAuth)) {
            return webSocket.send(ByteString.of(stream.toByteArray()));
        }
        return false;
    }

    @Locked
    private void doReconnect() {
        log.info("TNTClient API Reconnecting... Status: {}", state);

        if (state == State.RECONNECTING || state == State.AUTHORISING || state == State.CONNECTING) {
            if (reconnectingFuture != null) reconnectingFuture.cancel(true);
            if (authorisingFuture != null) authorisingFuture.cancel(true);

            if (webSocket != null) {
                webSocket.close(1000, null);

                webSocket = null;
            }

            reconnectingFuture = executorService.schedule(() -> {
                lastConnectTime = System.nanoTime();

                log.info("TNTClient API Connecting...");

                webSocket = client.newWebSocket(request, webSocketListener);
            }, reconnectDelayNanos - (System.nanoTime() - lastConnectTime), TimeUnit.NANOSECONDS);
        }
    }

    private enum State {
        RECONNECTING, CLOSED, CONNECTING, AUTHORISING, CONNECTED
    }
}