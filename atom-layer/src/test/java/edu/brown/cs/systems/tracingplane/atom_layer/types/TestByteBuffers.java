package edu.brown.cs.systems.tracingplane.atom_layer.types;

import static org.junit.Assert.assertEquals;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Random;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestByteBuffers {
    
    @Rule
    public final ExpectedException exception = ExpectedException.none();
    
    @Test
    public void testCopyToAdvancesPositionOnlyInDest() {
        ByteBuffer src = ByteBuffer.allocate(10);
        ByteBuffer dest = ByteBuffer.allocate(10);
        
        ByteBuffers.copyTo(src, dest);
        assertEquals(0, src.position());
        assertEquals(10, dest.position());
    }
    
    @Test
    public void testCopyNulls() {
        ByteBuffers.copyTo(null, ByteBuffer.allocate(0));
        ByteBuffers.copyTo(ByteBuffer.allocate(0), null);
        ByteBuffers.copyTo(null, null);
    }
    
    @Test
    public void testCopyInsufficientSpace() {
        ByteBuffer src = ByteBuffer.allocate(10);
        ByteBuffer dest = ByteBuffer.allocate(5);
        
        exception.expect(BufferOverflowException.class);
        ByteBuffers.copyTo(src, dest);
    }
    
    @Test
    public void testCopyCorrect() {
        Random r = new Random(0);
        
        ByteBuffer src = ByteBuffer.allocate(200);
        r.nextBytes(src.array());
        src.position(77);
        src.limit(127);
        
        ByteBuffer dest = ByteBuffer.allocate(500);
        r.nextBytes(dest.array());
        dest.position(133);
        dest.limit(450);
        
        ByteBuffers.copyTo(src, dest);
        assertEquals(77, src.position());
        assertEquals(127, src.limit());
        assertEquals(183, dest.position());
        assertEquals(450, dest.limit());
        
        dest.position(133);
        dest.limit(183);
        assertEquals(src, dest);
    }
    
    @Test
    public void testCopyWithPrefix() {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(0, 55);
        
        byte prefix = 109;
        
        ByteBuffer copied = ByteBuffers.copyWithPrefix(prefix, buf);
        
        assertEquals(prefix, copied.get());
        assertEquals(55, copied.getInt());
        
    }

}
