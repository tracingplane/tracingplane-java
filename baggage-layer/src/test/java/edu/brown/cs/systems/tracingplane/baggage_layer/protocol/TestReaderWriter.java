package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;

public class TestReaderWriter {
    
    private static final Random r = new Random(0);
    
    private static ByteBuffer randomBytes(int length) {
        byte[] bytes = new byte[length];
        r.nextBytes(bytes);
        return ByteBuffer.wrap(bytes);
    }
    
    private static BagKey.Indexed indexed(int index) {
        return (BagKey.Indexed) BagKey.indexed(index);
    }
    
    private static List<ByteBuffer> writeBag(BagKey key, ByteBuffer... contents) {
        BaggageWriter writer = BaggageWriter.create();
        writer.enter(key);
        for (ByteBuffer content : contents) {
            ByteBuffers.copyTo(content, writer.newDataAtom(content.remaining()));
        }
        writer.exit();
        return writer.atoms();
    }
    
    private static List<ByteBuffer> writeBag(BagKey a, BagKey b, ByteBuffer... contents) {
        BaggageWriter writer = BaggageWriter.create();
        writer.enter(a);
        writer.enter(b);
        for (ByteBuffer content : contents) {
            ByteBuffers.copyTo(content, writer.newDataAtom(content.remaining()));
        }
        writer.exit();
        writer.exit();
        return writer.atoms();
    }
    
    @Before
    public void setUp() throws Exception {
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
    
    @Test
    public void testReadNestedBag() {
        BagKey keyA = indexed(3);
        BagKey keyB = indexed(5);
        ByteBuffer contents = randomBytes(10);
        List<ByteBuffer> atoms = writeBag(keyA, keyB, contents);

        BaggageReader reader = BaggageReader.create(atoms);
        
        assertTrue(reader.hasChild());
        assertTrue(reader.enter(keyA));
        assertTrue(reader.hasChild());
        assertFalse(reader.enter(keyA));
        assertTrue(reader.enter(keyB));
        assertFalse(reader.hasChild());
        assertTrue(reader.hasData());
        assertEquals(contents, reader.nextData());
    }
    
    @Test
    public void testReadMultipleData() {
        BagKey key = indexed(3);
        ByteBuffer contentsA = randomBytes(10);
        ByteBuffer contentsB = randomBytes(10);
        ByteBuffer contentsC = randomBytes(10);
        List<ByteBuffer> atoms = writeBag(key, contentsA, contentsB, contentsC);

        BaggageReader reader = BaggageReader.create(atoms);
        
        assertTrue(reader.hasChild());
        assertTrue(reader.enter(key));
        assertTrue(reader.hasData());
        assertEquals(contentsA, reader.nextData());
        assertEquals(contentsB, reader.nextData());
        assertEquals(contentsC, reader.nextData());
    }
    
    @Test
    public void testEnterSiblingBags() {
        BagKey keyA = indexed(100);
        BagKey keyB = indexed(Integer.MAX_VALUE);
        ByteBuffer contentsA = randomBytes(10);
        ByteBuffer contentsB = randomBytes(10);

        BaggageWriter writer = BaggageWriter.create();
        writer.enter(keyA);
        ByteBuffers.copyTo(contentsA, writer.newDataAtom(contentsA.remaining()));
        writer.exit();
        writer.enter(keyB);
        ByteBuffers.copyTo(contentsB, writer.newDataAtom(contentsB.remaining()));
        writer.exit();
        
        BaggageReader reader = BaggageReader.create(writer.atoms());

        assertFalse(reader.hasData());
        assertTrue(reader.hasChild());
        
        assertTrue(reader.enter(keyA));
        assertTrue(reader.hasData());
        assertEquals(contentsA, reader.nextData());
        assertFalse(reader.hasData());
        assertFalse(reader.hasChild());
        reader.exit();
        
        assertFalse(reader.hasData());
        assertTrue(reader.hasChild());
        assertFalse(reader.enter(keyA));
        assertTrue(reader.enter(keyB));
        assertEquals(contentsB, reader.nextData());
        assertFalse(reader.hasData());
        assertFalse(reader.hasChild());
        reader.exit();

        assertFalse(reader.hasData());
        assertFalse(reader.hasChild());
    }
    
    @Test
    public void testEnterSiblingBags2() {
        BagKey keyA = indexed(100);
        BagKey keyB = indexed(Integer.MAX_VALUE);
        ByteBuffer contentsA = randomBytes(10);
        ByteBuffer contentsB = randomBytes(10);

        BaggageWriter writer = BaggageWriter.create();
        writer.enter(keyA);
        ByteBuffers.copyTo(contentsA, writer.newDataAtom(contentsA.remaining()));
        writer.exit();
        writer.enter(keyB);
        ByteBuffers.copyTo(contentsB, writer.newDataAtom(contentsB.remaining()));
        writer.exit();
        
        BaggageReader reader = BaggageReader.create(writer.atoms());

        assertFalse(reader.hasData());
        assertTrue(reader.hasChild());
        
        assertEquals(keyA, reader.enter());
        assertTrue(reader.hasData());
        assertEquals(contentsA, reader.nextData());
        assertFalse(reader.hasData());
        assertFalse(reader.hasChild());
        reader.exit();
        
        assertFalse(reader.hasData());
        assertTrue(reader.hasChild());
        assertEquals(keyB, reader.enter());
        assertEquals(contentsB, reader.nextData());
        assertFalse(reader.hasData());
        assertFalse(reader.hasChild());
        reader.exit();

        assertFalse(reader.hasData());
        assertFalse(reader.hasChild());
    }
    
    @Test
    public void testEmptyBaggage() {
        BaggageReader reader = BaggageReader.create((Iterator<ByteBuffer>) null);
        assertFalse(reader.hasNext());
        assertFalse(reader.hasData());
        assertFalse(reader.hasChild());
        
    }

}
