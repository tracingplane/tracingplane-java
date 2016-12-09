package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayerException.BaggageLayerRuntimeException;

/**
 * Tests an empty baggage
 */
public class TestBaggage02_EmptyBaggage extends BaggageTestCase {
    
    private BaggageReader makeBaggage() {
        BaggageWriter writer = BaggageWriter.create();
        return BaggageReader.create(writer.atoms());
    }
    
    @Test
    public void testNextData() {
        BaggageReader reader = makeBaggage();
        assertEquals(null, reader.nextData());
    }
    
    @Test
    public void testHasNext() {
        BaggageReader reader = makeBaggage();
        assertFalse(reader.hasNext());
    }
    
    @Test
    public void testHasData() {
        BaggageReader reader = makeBaggage();
        assertFalse(reader.hasData());
    }
    
    @Test
    public void testHasChild() {
        BaggageReader reader = makeBaggage();
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
    public void testExitThrowsException() {
        BaggageReader reader = makeBaggage();
        exception.expect(BaggageLayerRuntimeException.class);
        reader.exit();
    }
    
    @Test
    public void testNoUnprocessedData() {
        BaggageReader reader = makeBaggage();
        reader.nextData();
        assertNull(reader.unprocessedAtoms());
    }
    
    @Test
    public void testDidNotOverflow() {
        BaggageReader reader = makeBaggage();

        assertFalse(reader.didOverflow());
        reader.nextData();
        assertFalse(reader.didOverflow());
    }
    
    @Test
    public void testNoOverflow() {
        BaggageReader reader = makeBaggage();

        reader.nextData();
        assertNull(reader.overflowAtoms());
    }
}
