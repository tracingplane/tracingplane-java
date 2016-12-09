package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;
import org.junit.Test;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayerException.BaggageLayerRuntimeException;
import static org.junit.Assert.*;


public class TestBaggage01 extends BaggageTestCase {
    
    private final ByteBuffer payload = randomBytes(10);
    
    private BaggageReader makeBaggage() {
        BaggageWriter writer = BaggageWriter.create();
        ByteBuffers.copyTo(payload, writer.newDataAtom(payload.remaining()));
        return BaggageReader.create(writer.atoms());
    }
    
    @Test
    public void testNextData() {
        BaggageReader reader = makeBaggage();
        
        assertEquals(payload, reader.nextData());
        assertEquals(null, reader.nextData());
    }
    
    @Test
    public void testHasNext() {
        BaggageReader reader = makeBaggage();

        assertTrue(reader.hasNext());
        reader.nextData();
        assertFalse(reader.hasNext());
    }
    
    @Test
    public void testHasData() {
        BaggageReader reader = makeBaggage();
        
        assertTrue(reader.hasData());
        reader.nextData();
        assertFalse(reader.hasData());
    }
    
    @Test
    public void testHasChild() {
        BaggageReader reader = makeBaggage();

        assertFalse(reader.hasChild());
        reader.nextData();
        assertFalse(reader.hasChild());
    }
    
    @Test
    public void testEnter1() {
        BaggageReader reader = makeBaggage();

        assertNull(reader.enter());
    }
    
    @Test
    public void testEnter2() {
        BaggageReader reader = makeBaggage();

        assertFalse(reader.enter(indexed(3)));
    }
    
    @Test
    public void testEnterSkipsData() {
        BaggageReader reader = makeBaggage();

        reader.enter();
        assertFalse(reader.hasData());
        assertNull(reader.nextData());
    }
    
    @Test
    public void testEnterSkipsData2() {
        BaggageReader reader = makeBaggage();

        reader.enter(indexed(3));
        assertFalse(reader.hasData());
        assertNull(reader.nextData());
    }
    
    @Test
    public void testFinishClearsBag() {
        BaggageReader reader = makeBaggage();

        assertTrue(reader.hasData());
        reader.finish();
        assertFalse(reader.hasData());
    }
    
    @Test
    public void testExitThrowsException() {
        BaggageReader reader = makeBaggage();

        assertTrue(reader.hasData());
        
        exception.expect(BaggageLayerRuntimeException.class);
        reader.exit();
    }
}
