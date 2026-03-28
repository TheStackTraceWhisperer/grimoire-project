package com.grimoire.shared.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.fory.Fory;
import org.apache.fory.ThreadLocalFory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.Language;

/**
 * Decoder that deserializes objects using Apache Fory.
 */
public class ForyDecoder extends LengthFieldBasedFrameDecoder {
    /** Maximum allowed frame size for inbound payloads (10 MB). */
    private static final int MAX_FRAME_LENGTH = 10 * 1024 * 1024;
    /** Thread-safe Fory serializer/deserializer shared by decoder instances. */
    private static final ThreadSafeFory FORY = new ThreadLocalFory(classLoader -> {
        return Fory.builder()
                .withLanguage(Language.JAVA)
                .requireClassRegistration(false)
                .build();
    });

    /** Creates a length-prefixed frame decoder (4-byte length header). */
    public ForyDecoder() {
        super(MAX_FRAME_LENGTH, 0, 4, 0, 4);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }

        try {
            byte[] bytes = new byte[frame.readableBytes()];
            frame.readBytes(bytes);
            return FORY.deserialize(bytes);
        } finally {
            frame.release();
        }
    }
}
