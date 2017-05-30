package brown.tracingplane.baggageprotocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import org.junit.Test;
import com.google.common.collect.Lists;
import brown.tracingplane.atomlayer.ByteBuffers;
import brown.tracingplane.baggageprotocol.BaggageLayerException.BaggageLayerRuntimeException;

/**
 * Tests a simple baggage containing one child bags and some data elements in the bag
 */
public class TestBaggage04_OneChild extends BaggageTestCase {

    private final BagKey.Indexed bagKey = indexed(4);
    private final ByteBuffer[] payloads = { randomBytes(3), randomBytes(1), randomBytes(0) };
    private final ByteBuffer[] allAtoms =
            { headerAtom(bagKey, 0), dataAtom(payloads[0]), dataAtom(payloads[1]), dataAtom(payloads[2]) };

    private BaggageReader makeBaggage() {
        BaggageWriter writer = BaggageWriter.create();
        writer.enter(bagKey);
        for (ByteBuffer payload : payloads) {
            ByteBuffers.copyTo(payload, writer.newDataAtom(payload.remaining()));
        }
        writer.exit();
        return BaggageReader.create(writer.atoms());
    }

    @Test
    public void testExplicitEnter() {
        BaggageReader reader = makeBaggage();
        assertTrue(reader.enter(bagKey));
        assertFalse(reader.enter(bagKey));
        reader.exit();
        assertFalse(reader.enter(bagKey));
    }

    @Test
    public void testImplicitEnter() {
        BaggageReader reader = makeBaggage();
        assertEquals(bagKey, reader.enter());
        assertNull(reader.enter());
        reader.exit();
        assertNull(reader.enter());
    }

    @Test
    public void testNextDataExplicitEntry() {
        BaggageReader reader = makeBaggage();

        assertNull(reader.nextData());
        reader.enter(bagKey);
        for (ByteBuffer payload : payloads) {
            assertEquals(payload, reader.nextData());
        }
        assertNull(reader.nextData());
        reader.exit();
        assertNull(reader.nextData());
    }

    @Test
    public void testNextDataImplicitEntry() {
        BaggageReader reader = makeBaggage();

        assertNull(reader.nextData());
        reader.enter();
        for (ByteBuffer payload : payloads) {
            assertEquals(payload, reader.nextData());
        }
        assertNull(reader.nextData());
        reader.exit();
        assertNull(reader.nextData());
    }

    @Test
    public void testHasNext() {
        BaggageReader reader = makeBaggage();

        assertTrue(reader.hasNext());
        reader.enter(bagKey);
        for (int i = 0; i < payloads.length; i++) {
            assertTrue(reader.hasNext());
            reader.nextData();
        }
        assertFalse(reader.hasNext());
        reader.exit();
        assertFalse(reader.hasNext());
    }

    @Test
    public void testHasData() {
        BaggageReader reader = makeBaggage();

        assertFalse(reader.hasData());
        reader.enter(bagKey);
        for (int i = 0; i < payloads.length; i++) {
            assertTrue(reader.hasData());
            reader.nextData();
        }
        assertFalse(reader.hasData());
        reader.exit();
        assertFalse(reader.hasData());
    }

    @Test
    public void testHasChild() {
        BaggageReader reader = makeBaggage();

        assertTrue(reader.hasChild());
        reader.enter(bagKey);
        for (int i = 0; i < payloads.length; i++) {
            assertFalse(reader.hasChild());
            reader.nextData();
        }
        assertFalse(reader.hasChild());
        reader.exit();
        assertFalse(reader.hasChild());
    }

    @Test
    public void testEnterEarlierBagExplicit() {
        BaggageReader reader = makeBaggage();

        assertFalse(reader.enter(indexed(bagKey.index - 1)));
        assertTrue(reader.enter(bagKey));
    }

    @Test
    public void testEnterEarlierBagImplicit() {
        BaggageReader reader = makeBaggage();

        assertFalse(reader.enter(indexed(bagKey.index - 1)));
        assertEquals(bagKey, reader.enter());
    }

    @Test
    public void testEnterLaterBagExplicit() {
        BaggageReader reader = makeBaggage();

        assertFalse(reader.enter(indexed(bagKey.index + 1)));
        assertFalse(reader.enter(bagKey));
    }

    @Test
    public void testEnterLaterBagImplicit() {
        BaggageReader reader = makeBaggage();

        assertFalse(reader.enter(indexed(bagKey.index + 1)));
        assertNull(reader.enter());
    }

    @Test
    public void testEnterSkipsChildData() {
        BaggageReader reader = makeBaggage();

        assertEquals(bagKey, reader.enter());
        assertNull(reader.enter());
        assertFalse(reader.hasData());
        assertNull(reader.nextData());
    }

    @Test
    public void testFinishClearsBag() {
        BaggageReader reader = makeBaggage();

        assertTrue(reader.hasChild());
        reader.finish();
        assertFalse(reader.hasChild());
    }

    @Test
    public void testExitThrowsException() {
        BaggageReader reader = makeBaggage();

        reader.enter();
        reader.exit();

        exception.expect(BaggageLayerRuntimeException.class);
        reader.exit();
    }

    @Test
    public void testFinishMarksAllAtomsUnprocessed() {
        BaggageReader reader = makeBaggage();
        
        reader.finish();

        assertEquals(Lists.newArrayList(allAtoms), reader.unprocessedAtoms());
    }

    @Test
    public void testFinishMarksAllAtomsUnprocessedEvenWithEnter() {
        BaggageReader reader = makeBaggage();
        
        reader.enter(bagKey);
        reader.finish();

        assertEquals(Lists.newArrayList(allAtoms), reader.unprocessedAtoms());
    }

    @Test
    public void testEnterLaterBagMarksAllAtomsUnprocessed() {
        BaggageReader reader = makeBaggage();
        
        reader.enter(indexed(bagKey.index + 1));
        reader.finish();

        assertEquals(Lists.newArrayList(allAtoms), reader.unprocessedAtoms());
    }

    @Test
    public void testPartiallyProcessedDataIsNotMarkedAsUnprocessed() {
        BaggageReader reader = makeBaggage();
        
        reader.enter();
        reader.nextData();
        reader.exit();
        reader.finish();
        
        assertNull(reader.unprocessedAtoms());
        assertFalse(reader.hasNext());
    }

    @Test
    public void testDidNotOverflow() {
        BaggageReader reader = makeBaggage();

        assertFalse(reader.didOverflow());
        reader.enter();
        for (int i = 0; i < payloads.length; i++) {
            assertFalse(reader.didOverflow());
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
