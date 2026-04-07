package com.grimoire.infra.network;

import com.grimoire.infra.network.codec.ForyDecoder;
import com.grimoire.infra.network.codec.ForyEncoder;
import com.grimoire.infra.network.handler.GameLogicHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Constructs the byte-processing pipeline for each new client connection.
 *
 * <p>
 * Injects the {@link ForyDecoder}/{@link ForyEncoder} codecs (which handle
 * length-field framing internally) and the shared {@link GameLogicHandler} that
 * bridges decoded packets into the ECS {@code GameCommandQueue}.
 * </p>
 *
 * <p>
 * New codec instances are created per channel because {@link ForyDecoder}
 * extends {@code LengthFieldBasedFrameDecoder} which maintains per-channel
 * cumulation state.
 * </p>
 */
@Singleton
public class GameChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final GameLogicHandler gameLogicHandler;

    /**
     * Creates a channel initializer.
     *
     * @param gameLogicHandler
     *            the shared game logic handler (sharable)
     */
    @Inject
    public GameChannelInitializer(GameLogicHandler gameLogicHandler) {
        super();
        this.gameLogicHandler = gameLogicHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // 1. Serialization with built-in TCP framing (length-field codec)
        // ForyDecoder extends LengthFieldBasedFrameDecoder — handles
        // frame decoding and Fory deserialization in one step.
        // ForyEncoder writes a 4-byte length prefix + serialized payload.
        // New instances per channel: decoder maintains cumulation state.
        pipeline.addLast("foryDecoder", new ForyDecoder());
        pipeline.addLast("foryEncoder", new ForyEncoder());

        // 2. Application Logic Bridge (sharable — one instance for all channels)
        pipeline.addLast("gameLogicHandler", gameLogicHandler);
    }
}
