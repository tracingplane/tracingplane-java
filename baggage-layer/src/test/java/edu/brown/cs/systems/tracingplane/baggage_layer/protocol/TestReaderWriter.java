package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import junit.framework.TestCase;

public class TestReaderWriter extends TestCase {
    
    private static final Random r = new Random(0);
    
    private static ByteBuffer randomBytes(int length) {
        byte[] bytes = new byte[length];
        r.nextBytes(bytes);
        return ByteBuffer.wrap(bytes);
    }
    
    private static BagKey.Indexed indexed(int index) {
        return (BagKey.Indexed) BagKey.indexed(index);
    }
    
    private static List<ByteBuffer> writeBag(BagKey key, ByteBuffer contents) {
        BaggageWriter writer = BaggageWriter.create();
        writer.enter(key);
        ByteBuffers.copyTo(contents, writer.newDataAtom(contents.remaining()));
        writer.exit();
        return writer.atoms();
    }
    
    @Override
    protected void setUp() throws Exception {
        r.setSeed(0);
    };
    
    @Test
    public void testWriteReadSimple() {        
        BaggageWriter writer = BaggageWriter.create();
        
        int length = 10;
        ByteBuffer randomBytes = randomBytes(length);

        ByteBuffers.copyTo(randomBytes, writer.newDataAtom(length));
        
        List<ByteBuffer> atoms = writer.atoms();
        
        BaggageReader reader = BaggageReader.create(atoms);
        
        assertTrue(reader.hasNext());
        assertTrue(reader.hasData());
        assertFalse(reader.hasChild());
        assertEquals(randomBytes, reader.nextData());
        assertFalse(reader.hasData());
        assertFalse(reader.hasChild());
        assertFalse(reader.hasNext());
        assertNull(reader.nextData());
        
        reader.finish();
        assertNull(reader.overflowAtoms());
        assertNull(reader.unprocessedAtoms());
    }
    
    @Test
    public void testWriteReadBagSimple() {        
        BagKey key = indexed(4);
        ByteBuffer contents = randomBytes(10);
        List<ByteBuffer> atoms = writeBag(key, contents);
        
        BaggageReader reader = BaggageReader.create(atoms);
        
        assertTrue(reader.hasNext());
        assertFalse(reader.hasData());
        assertTrue(reader.hasChild());
        
        assertNull(reader.nextData());
        assertEquals(key, reader.enter());

        assertTrue(reader.hasNext());
        assertTrue(reader.hasData());
        assertFalse(reader.hasChild());
        
        assertEquals(contents, reader.nextData());
        assertNull(reader.nextData());
        
        assertFalse(reader.hasData());
        assertFalse(reader.hasChild());
        assertFalse(reader.hasNext());
        
        reader.exit();
        assertNull(reader.nextData());

        assertFalse(reader.hasData());
        assertFalse(reader.hasChild());
        assertFalse(reader.hasNext());
        
        reader.finish();
        
        assertNull(reader.overflowAtoms());
        assertNull(reader.unprocessedAtoms());
    }
    
    @Test
    public void testWriteReadKnownBagSimple() {   
        BagKey key = indexed(4);
        ByteBuffer contents = randomBytes(10);
        List<ByteBuffer> atoms = writeBag(key, contents);
        
        BaggageReader reader = BaggageReader.create(atoms);
        
        assertTrue(reader.hasNext());
        assertFalse(reader.hasData());
        assertTrue(reader.hasChild());
        
        assertNull(reader.nextData());
        assertTrue(reader.enter(key));

        assertTrue(reader.hasNext());
        assertTrue(reader.hasData());
        assertFalse(reader.hasChild());
        
        assertEquals(contents, reader.nextData());
        assertNull(reader.nextData());
        
        assertFalse(reader.hasData());
        assertFalse(reader.hasChild());
        assertFalse(reader.hasNext());
        
        reader.exit();
        assertNull(reader.nextData());

        assertFalse(reader.hasData());
        assertFalse(reader.hasChild());
        assertFalse(reader.hasNext());
        
        reader.finish();
        
        assertNull(reader.overflowAtoms());
        assertNull(reader.unprocessedAtoms());
    }
    
    @Test
    public void testReadEarlierBagsDoesNotSkipLaterBags() {
        BagKey key = indexed(3);
        ByteBuffer contents = randomBytes(10);
        List<ByteBuffer> atoms = writeBag(key, contents);
        
        BaggageReader reader = BaggageReader.create(atoms);

        assertFalse(reader.enter(indexed(1)));
        assertFalse(reader.enter(indexed(2)));
        assertTrue(reader.enter(indexed(3)));
    }
    
    @Test
    public void testReadLaterBagSkipsEarlierBags() {
        BagKey keyA = indexed(3);
        BagKey keyB = indexed(5);
        ByteBuffer contents = randomBytes(10);
        List<ByteBuffer> atoms = writeBag(keyA, contents);
        
        BaggageReader reader = BaggageReader.create(atoms);
        
        assertTrue(reader.hasChild());
        assertFalse(reader.enter(keyB));
        assertFalse(reader.hasChild());
    }

}
