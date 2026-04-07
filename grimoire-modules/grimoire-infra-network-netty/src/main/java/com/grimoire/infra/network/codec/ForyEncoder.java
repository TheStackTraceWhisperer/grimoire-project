package com.grimoire.infra.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.fory.Fory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.Language;

/**
 * Netty encoder that serialises objects using Apache Fory.
 *
 * <p>
 * Writes a 4-byte big-endian length prefix followed by the Fory-serialised
 * payload. The corresponding {@link ForyDecoder} uses
 * {@link io.netty.handler.codec.LengthFieldBasedFrameDecoder} to frame incoming
 * data.
 * </p>
 *
 * <p>
 * <strong>Thread-safety:</strong> Uses {@link ThreadSafeFory} with internal
 * locking so Netty Epoll workers can safely serialise concurrently without
 * buffer corruption.
 * </p>
 */
public class ForyEncoder extends MessageToByteEncoder<Object> {

    /**
     * Thread-safe Fory instance with internal synchronisation.
     */
    private static final ThreadSafeFory FORY = Fory.builder()
            .withLanguage(Language.JAVA)
            .requireClassRegistration(false)
            .buildThreadSafeFory();

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) {
        byte[] bytes = FORY.serialize(msg);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
