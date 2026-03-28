package com.grimoire.client.network;

import com.grimoire.shared.protocol.GamePacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Client-side packet handler for the network channel.
 */
@Slf4j
public class ClientLogicHandler extends SimpleChannelInboundHandler<GamePacket> {
    
    private final NetworkClient networkClient;
    
    public ClientLogicHandler(NetworkClient networkClient) {
        this.networkClient = networkClient;
    }
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GamePacket packet) {
        log.debug("Received packet: {}", packet.type());
        networkClient.onPacketReceived(packet);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("Disconnected from server");
        networkClient.onDisconnected();
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Network error", cause);
        ctx.close();
    }
}
