package com.grimoire.infra.network.codec;

import com.grimoire.contracts.wire.protocol.GamePacket;
import com.grimoire.contracts.wire.protocol.PacketType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Round-trip tests for {@link ForyEncoder} and {@link ForyDecoder}.
 */
class ForyCodecTest {

    @Test
    void gamePacketRoundTrips() {
        EmbeddedChannel channel = new EmbeddedChannel(new ForyEncoder(), new ForyDecoder());

        GamePacket original = new GamePacket(PacketType.C2S_CHAT_MESSAGE, "hello world");
        channel.writeOutbound(original);
        channel.writeInbound((Object) channel.readOutbound());

        Object decoded = channel.readInbound();
        assertThat(decoded).isInstanceOf(GamePacket.class);

        GamePacket result = (GamePacket) decoded;
        assertThat(result.type()).isEqualTo(PacketType.C2S_CHAT_MESSAGE);
        assertThat(result.payload()).isEqualTo("hello world");
    }

    @Test
    void nullPayloadRoundTrips() {
        EmbeddedChannel channel = new EmbeddedChannel(new ForyEncoder(), new ForyDecoder());

        GamePacket original = new GamePacket(PacketType.S2C_ENTITY_DESPAWN, null);
        channel.writeOutbound(original);
        channel.writeInbound((Object) channel.readOutbound());

        Object decoded = channel.readInbound();
        assertThat(decoded).isInstanceOf(GamePacket.class);

        GamePacket result = (GamePacket) decoded;
        assertThat(result.type()).isEqualTo(PacketType.S2C_ENTITY_DESPAWN);
        assertThat(result.payload()).isNull();
    }

    @Test
    void plainStringRoundTrips() {
        EmbeddedChannel channel = new EmbeddedChannel(new ForyEncoder(), new ForyDecoder());

        String original = "test payload";
        channel.writeOutbound(original);
        channel.writeInbound((Object) channel.readOutbound());

        Object decoded = channel.readInbound();
        assertThat(decoded).isEqualTo("test payload");
    }

    @Test
    void encoderWritesLengthPrefixAndPayload() {
        EmbeddedChannel channel = new EmbeddedChannel(new ForyEncoder());

        channel.writeOutbound("data");
        ByteBuf encoded = channel.readOutbound();

        assertThat(encoded).isNotNull();
        // First 4 bytes = length prefix
        int payloadLength = encoded.readInt();
        assertThat(payloadLength).isPositive();
        assertThat(encoded.readableBytes()).isEqualTo(payloadLength);

        encoded.release();
    }

    @Test
    void decoderReturnsNullForIncompleteFrame() {
        ForyDecoder decoder = new ForyDecoder();
        EmbeddedChannel channel = new EmbeddedChannel(decoder);

        // Write only 2 bytes — not enough for the 4-byte length header
        ByteBuf incomplete = Unpooled.buffer(2);
        incomplete.writeShort(0);
        channel.writeInbound(incomplete);

        Object result = channel.readInbound();
        assertThat(result).isNull();
    }

    @Test
    void multiplePacketsRoundTripInSequence() {
        EmbeddedChannel channel = new EmbeddedChannel(new ForyEncoder(), new ForyDecoder());

        GamePacket first = new GamePacket(PacketType.C2S_CHAT_MESSAGE, "msg1");
        GamePacket second = new GamePacket(PacketType.C2S_MOVEMENT_INTENT, "msg2");

        channel.writeOutbound(first);
        channel.writeOutbound(second);

        channel.writeInbound((Object) channel.readOutbound());
        channel.writeInbound((Object) channel.readOutbound());

        GamePacket decoded1 = (GamePacket) channel.readInbound();
        GamePacket decoded2 = (GamePacket) channel.readInbound();

        assertThat(decoded1.type()).isEqualTo(PacketType.C2S_CHAT_MESSAGE);
        assertThat(decoded1.payload()).isEqualTo("msg1");
        assertThat(decoded2.type()).isEqualTo(PacketType.C2S_MOVEMENT_INTENT);
        assertThat(decoded2.payload()).isEqualTo("msg2");
    }
}
