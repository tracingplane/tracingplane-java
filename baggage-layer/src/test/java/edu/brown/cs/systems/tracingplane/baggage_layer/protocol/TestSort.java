package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.Test;
import com.google.common.collect.Lists;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayerException.BaggageLayerRuntimeException;

/**
 * Tests a simple baggage containing no child bags and multiple data elements
 */
public class TestSort extends BaggageTestCase {

    @Test
    public void testSimpleNoSort() {
        ByteBuffer payloadA = ByteBuffer.allocate(4);
        payloadA.putInt(0, 100);

        ByteBuffer payloadB = ByteBuffer.allocate(4);
        payloadB.putInt(0, 55);
        
        BaggageWriter writer = BaggageWriter.create();
        writer.writeBytes(payloadA);
        writer.writeBytes(payloadB);
        
        List<ByteBuffer> atoms = writer.atoms();
        
        BaggageReader reader = BaggageReader.create(atoms);
        ByteBuffer firstRead = reader.nextData();
        ByteBuffer secondRead = reader.nextData();
        
        assertEquals(payloadA, firstRead);
        assertEquals(payloadB, secondRead);
    }

    @Test
    public void testSimpleSort() {
        ByteBuffer payloadA = ByteBuffer.allocate(4);
        payloadA.putInt(0, 100);

        ByteBuffer payloadB = ByteBuffer.allocate(4);
        payloadB.putInt(0, 55);
        
        BaggageWriter writer = BaggageWriter.create();
        writer.writeBytes(payloadA);
        writer.writeBytes(payloadB);
        writer.sortData();
        
        List<ByteBuffer> atoms = writer.atoms();
        
        BaggageReader reader = BaggageReader.create(atoms);
        ByteBuffer firstRead = reader.nextData();
        ByteBuffer secondRead = reader.nextData();
        
        assertEquals(payloadB, firstRead);
        assertEquals(payloadA, secondRead);
    }

    @Test
    public void testMultiSort() {
        int numbufs = 100;
        ByteBuffer[] bufs = new ByteBuffer[numbufs];
        for (int i = 0; i < numbufs; i++) {
            bufs[i] = ByteBuffer.allocate(4);
            bufs[i].putInt(0, numbufs-i-1);
        }
        
        BaggageWriter writer = BaggageWriter.create();
        for (int i = 0; i < numbufs; i++) {
            writer.writeBytes(bufs[i]);
        }
        writer.sortData();
        
        List<ByteBuffer> atoms = writer.atoms();
        
        BaggageReader reader = BaggageReader.create(atoms);
        for (int i = 0; i < numbufs; i++) {
            ByteBuffer bufRead = reader.nextData();
            assertEquals(bufs[numbufs-i-1], bufRead);
        }
    }
    
    @Test
    public void testNoSortChildBag() {
        ByteBuffer payloadA = ByteBuffer.allocate(4);
        payloadA.putInt(0, 100);

        ByteBuffer payloadB = ByteBuffer.allocate(4);
        payloadB.putInt(0, 55);
        
        BaggageWriter writer = BaggageWriter.create();
        writer.writeBytes(payloadA);
        writer.writeBytes(payloadB);
        
        BagKey key = BagKey.indexed(1);
        
        writer.enter(key);
        writer.writeBytes(payloadA);
        writer.writeBytes(payloadB);
        writer.exit();
        
        List<ByteBuffer> atoms = writer.atoms();
        BaggageReader reader = BaggageReader.create(atoms);
        
        assertEquals(payloadA, reader.nextData());
        assertEquals(payloadB, reader.nextData());
        assertTrue(reader.enter(key));
        assertTrue(reader.hasData());
        assertEquals(payloadA, reader.nextData());
        assertEquals(payloadB, reader.nextData());
    }
    
    @Test
    public void testSortChildBag() {
        ByteBuffer payloadA = ByteBuffer.allocate(4);
        payloadA.putInt(0, 100);

        ByteBuffer payloadB = ByteBuffer.allocate(4);
        payloadB.putInt(0, 55);
        
        BaggageWriter writer = BaggageWriter.create();
        writer.writeBytes(payloadA);
        writer.writeBytes(payloadB);
        
        BagKey key = BagKey.indexed(1);
        
        writer.enter(key);
        writer.writeBytes(payloadA);
        writer.writeBytes(payloadB);
        writer.sortData();
        writer.exit();
        
        BaggageReader reader = BaggageReader.create(writer.atoms());
        
        assertEquals(payloadA, reader.nextData());
        assertEquals(payloadB, reader.nextData());
        assertTrue(reader.enter(key));
        assertEquals(payloadB, reader.nextData());
        assertEquals(payloadA, reader.nextData());
    }
    
    @Test
    public void testSortNoData() {
        ByteBuffer payloadA = ByteBuffer.allocate(4);
        payloadA.putInt(0, 100);

        ByteBuffer payloadB = ByteBuffer.allocate(4);
        payloadB.putInt(0, 55);
        
        BaggageWriter writer = BaggageWriter.create();
        writer.writeBytes(payloadA);
        writer.writeBytes(payloadB);
        
        BagKey key = BagKey.indexed(1);
        
        writer.enter(key);
        writer.writeBytes(payloadA);
        writer.writeBytes(payloadB);
        writer.exit();
        writer.sortData();
        
        BaggageReader reader = BaggageReader.create(writer.atoms());
        
        assertEquals(payloadA, reader.nextData());
        assertEquals(payloadB, reader.nextData());
        assertTrue(reader.enter(key));
        assertEquals(payloadA, reader.nextData());
        assertEquals(payloadB, reader.nextData());
    }
}
