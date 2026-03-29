package com.grimoire.shared.codec;

import com.grimoire.shared.protocol.GamePacket;
import com.grimoire.shared.protocol.PacketType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class ForyCodecTest {
    
    @Test
    void testEncodeAndDecode() throws Exception {
        // Create test data
        record TestPayload(String message, int value) implements Serializable {}
        TestPayload payload = new TestPayload("Hello", 42);
        GamePacket originalPacket = new GamePacket(PacketType.C2S_CHAT_MESSAGE, payload);
        
        // Encode
        ForyEncoder encoder = new ForyEncoder();
        ByteBuf buffer = Unpooled.buffer();
        ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
        encoder.encode(ctx, originalPacket, buffer);
        
        // Verify length field was written
        assertTrue(buffer.readableBytes() > 4, "Buffer should contain length field and data");
        
        // Decode
        ForyDecoder decoder = new ForyDecoder();
        Object decodedObj = decoder.decode(ctx, buffer);
        
        // Verify
        assertNotNull(decodedObj);
        assertInstanceOf(GamePacket.class, decodedObj);
        GamePacket decodedPacket = (GamePacket) decodedObj;
        assertEquals(PacketType.C2S_CHAT_MESSAGE, decodedPacket.type());
        assertInstanceOf(TestPayload.class, decodedPacket.payload());
        TestPayload decodedPayload = (TestPayload) decodedPacket.payload();
        assertEquals("Hello", decodedPayload.message());
        assertEquals(42, decodedPayload.value());
    }
    
    @Test
    void testDecodeReturnsNullForIncompleteFrame() throws Exception {
        // Create incomplete buffer (length field only)
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeInt(100); // length field says 100 bytes, but no data
        
        ForyDecoder decoder = new ForyDecoder();
        ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
        
        // Decode should return null for incomplete frame
        Object result = decoder.decode(ctx, buffer);
        assertNull(result);
    }
    
    @Test
    void testEncodeSimpleString() throws Exception {
        String testString = "Test String";
        
        ForyEncoder encoder = new ForyEncoder();
        ByteBuf buffer = Unpooled.buffer();
        ChannelHandlerContext ctx = Mockito.mock(ChannelHandlerContext.class);
        encoder.encode(ctx, testString, buffer);
        
        // Verify data was written
        assertTrue(buffer.readableBytes() > 4);
        
        // Decode
        ForyDecoder decoder = new ForyDecoder();
        Object decoded = decoder.decode(ctx, buffer);
        
        assertEquals(testString, decoded);
    }
}
