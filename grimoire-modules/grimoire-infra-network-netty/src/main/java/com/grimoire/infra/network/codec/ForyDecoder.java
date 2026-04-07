package com.grimoire.infra.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.fory.Fory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.Language;

/**
 * Netty decoder that deserialises objects using Apache Fory.
 *
 * <p>
 * Extends {@link LengthFieldBasedFrameDecoder} to handle the 4-byte length
 * prefix written by {@link ForyEncoder}. Maximum frame length is 10 MB.
 * </p>
 *
 * <p>
 * <strong>Thread-safety:</strong> Uses {@link ThreadSafeFory} with internal
 * locking — safe for use in a multi-threaded Netty pipeline.
 * </p>
 */
public class ForyDecoder extends LengthFieldBasedFrameDecoder {

    /** Maximum frame size: 10 MB. */
    private static final int MAX_FRAME_LENGTH = 10 * 1024 * 1024;

    /** Thread-safe Fory instance with internal synchronisation. */
    private static final ThreadSafeFory FORY = Fory.builder()
            .withLanguage(Language.JAVA)
            .requireClassRegistration(false)
            .buildThreadSafeFory();

    /**
     * Creates a Fory decoder with default frame settings.
     *
     * <p>
     * Frame layout: {@code [4-byte length] [payload]}, where the length field is
     * stripped before the payload reaches this decoder's {@code decode} method.
     * </p>
     */
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
