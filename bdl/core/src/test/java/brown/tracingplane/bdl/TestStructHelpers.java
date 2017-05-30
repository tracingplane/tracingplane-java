package brown.tracingplane.bdl;

import static org.junit.Assert.assertEquals;
import java.nio.ByteBuffer;
import org.junit.Test;

public class TestStructHelpers {
    
    @Test
    public void testStructReadWrite() throws Exception {
        ByteBuffer buf = ByteBuffer.allocate(100);
        
        StructHelpers.stringWriter.writeTo(buf, "hello");
        StructHelpers.stringWriter.writeTo(buf, "again");
        
        buf.flip();

        assertEquals("hello", StructHelpers.stringReader.readFrom(buf));
        assertEquals("again", StructHelpers.stringReader.readFrom(buf));
    }

}
