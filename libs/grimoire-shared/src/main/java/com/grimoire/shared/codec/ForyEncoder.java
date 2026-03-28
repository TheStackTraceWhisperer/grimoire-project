package com.grimoire.shared.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.apache.fory.Fory;
import org.apache.fory.ThreadLocalFory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.Language;

/**
 * Encoder that serializes objects using Apache Fory.
 */
public class ForyEncoder extends MessageToByteEncoder<Object> {
    /** Thread-safe Fory serializer shared by encoder instances. */
    private static final ThreadSafeFory FORY = new ThreadLocalFory(classLoader -> {
        return Fory.builder()
                .withLanguage(Language.JAVA)
                .requireClassRegistration(false)
                .build();
    });

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        byte[] bytes = FORY.serialize(msg);
        out.writeInt(bytes.length);
        out.writeBytes(bytes);
    }
}
