package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import org.junit.Test;
import com.google.common.collect.Lists;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayerException.BaggageLayerRuntimeException;

/**
 * Tests a simple baggage containing no child bags and multiple data elements
 */
public class TestBaggage03_MultipleDataElements extends BaggageTestCase {

    private final ByteBuffer[] payloads = { randomBytes(10), randomBytes(15), randomBytes(20) };
    private final ByteBuffer[] payloadAtoms = { dataAtom(payloads[0]), dataAtom(payloads[1]), dataAtom(payloads[2]) };

    private BaggageReader makeBaggage() {
        BaggageWriter writer = BaggageWriter.create();
        for (ByteBuffer payload : payloads) {
            ByteBuffers.copyTo(payload, writer.newDataAtom(payload.remaining()));
        }
        return BaggageReader.create(writer.atoms());
    }

    @Test
    public void testNextData() {
        BaggageReader reader = makeBaggage();
        
        for (ByteBuffer payload : payloads) {
            assertEquals(payload, reader.nextData());
        }
        assertEquals(null, reader.nextData());
    }

    @Test
    public void testHasNext() {
        BaggageReader reader = makeBaggage();

        for (int i = 0; i < payloads.length; i++) {
            assertTrue(reader.hasNext());
            reader.nextData();
        }
        assertFalse(reader.hasNext());
    }

    @Test
    public void testHasData() {
        BaggageReader reader = makeBaggage();

        for (int i = 0; i < payloads.length; i++) {
            assertTrue(reader.hasData());
            reader.nextData();
        }
        assertFalse(reader.hasData());
    }

    @Test
    public void testHasChild() {
        BaggageReader reader = makeBaggage();

        for (int i = 0; i < payloads.length; i++) {
            assertFalse(reader.hasChild());
            reader.nextData();
        }
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

    @Test
    public void testFinishMarksDataAsUnprocessed() {
        BaggageReader reader = makeBaggage();

        assertEquals(Lists.newArrayList(payloadAtoms), reader.unprocessedAtoms());
    }
    
    @Test
    public void testEnterChildBagWithoutTouchingData() {
        BaggageReader reader = makeBaggage();
        
        reader.enter();
        reader.finish();

        assertEquals(Lists.newArrayList(payloadAtoms), reader.unprocessedAtoms());
    }

    @Test
    public void testPartiallyProcessedDataIsNotMarkedAsUnprocessed() {
        BaggageReader reader = makeBaggage();
        reader.nextData();
        assertNull(reader.unprocessedAtoms());
        assertFalse(reader.hasNext());
    }

    @Test
    public void testDidNotOverflow() {
        BaggageReader reader = makeBaggage();

        assertFalse(reader.didOverflow());
        for (int i = 0; i < payloads.length; i++) {
            reader.nextData();
        }
        assertFalse(reader.didOverflow());
    }

    @Test
    public void testNoOverflow() {
        BaggageReader reader = makeBaggage();

        for (int i = 0; i < payloads.length; i++) {
            reader.nextData();
        }
        assertNull(reader.overflowAtoms());
    }
}
