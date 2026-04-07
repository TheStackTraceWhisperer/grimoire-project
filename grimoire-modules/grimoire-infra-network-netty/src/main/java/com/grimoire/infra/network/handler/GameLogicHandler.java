package com.grimoire.infra.network.handler;

import com.grimoire.application.core.ecs.GameCommandQueue;
import com.grimoire.contracts.wire.protocol.GamePacket;
import com.grimoire.infra.network.registry.NetworkSessionRegistry;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sharable handler to bridge Netty I/O threads to the synchronous ECS
 * {@link GameCommandQueue}.
 *
 * <p>
 * Runs on Netty's async worker threads. Catches fully decoded
 * {@link GamePacket} objects and safely pushes them into the pure ECS
 * {@link GameCommandQueue} for processing on the next main engine tick.
 * </p>
 */
@Singleton
@ChannelHandler.Sharable
public class GameLogicHandler extends SimpleChannelInboundHandler<GamePacket> {

    private static final Logger LOG = LoggerFactory.getLogger(GameLogicHandler.class);

    private final GameCommandQueue commandQueue;
    private final NetworkSessionRegistry sessionRegistry;

    /**
     * Creates a game logic handler.
     *
     * @param commandQueue
     *            the ECS command queue for cross-thread communication
     * @param sessionRegistry
     *            the session registry for channel tracking
     */
    @Inject
    public GameLogicHandler(GameCommandQueue commandQueue,
            NetworkSessionRegistry sessionRegistry) {
        super();
        this.commandQueue = commandQueue;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("Client connected: {}", ctx.channel().remoteAddress());
        }
        sessionRegistry.registerChannel(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("Client disconnected: {}", ctx.channel().remoteAddress());
        }
        sessionRegistry.removeChannel(ctx.channel());

        // Push a disconnect event to the ECS so it can despawn the character
        // commandQueue.enqueue(new
        // NetworkDisconnectCommand(ctx.channel().id().asLongText()));

        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GamePacket packet) {
        // Log the inbound packet trace (Optional, use debug in production)
        if (LOG.isDebugEnabled()) {
            LOG.debug("Received packet type: {} from {}", packet.type(), ctx.channel().id());
        }

        // Delegate to the pure Application core.
        // We attach the ChannelId as a string so the ECS knows who sent it
        // without needing a Netty import.
        commandQueue.enqueueInboundPacket(ctx.channel().id().asShortText(), packet);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (LOG.isErrorEnabled()) {
            LOG.error("Network exception on channel {}", ctx.channel().id(), cause);
        }
        ctx.close();
    }
}
