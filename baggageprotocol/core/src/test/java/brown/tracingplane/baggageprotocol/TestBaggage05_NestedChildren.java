package brown.tracingplane.baggageprotocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Test;
import com.google.common.collect.Lists;
import brown.tracingplane.atomlayer.ByteBuffers;
import brown.tracingplane.baggageprotocol.BaggageLayerException.BaggageLayerRuntimeException;

/**
 * Tests a baggage containing a bag that contains a bag that contains ...
 */
public class TestBaggage05_NestedChildren extends BaggageTestCase {

    private final BagKey.Indexed first = indexed(4);
    private final BagKey.Indexed second = indexed(30);
    private final BagKey.Indexed third = indexed(-10);

    private final List<ByteBuffer> rootPayload = Lists.newArrayList(randomBytes(100));
    private final List<ByteBuffer> firstPayload = Lists.newArrayList(randomBytes(1222), randomBytes(1234));
    private final List<ByteBuffer> secondPayload = Lists.newArrayList(randomBytes(3), randomBytes(1), randomBytes(0));
    private final List<ByteBuffer> thirdPayload = Lists.newArrayList(randomBytes(5));

    private final List<ByteBuffer> allAtoms =
            Lists.newArrayList(dataAtom(rootPayload.get(0)), headerAtom(first, 0), dataAtom(firstPayload.get(0)),
                               dataAtom(firstPayload.get(1)), headerAtom(second, 1), dataAtom(secondPayload.get(0)),
                               dataAtom(secondPayload.get(1)), dataAtom(secondPayload.get(2)), headerAtom(third, 2),
                               dataAtom(thirdPayload.get(0)));

    private void writePayloads(BaggageWriter writer, List<ByteBuffer> payloads) {
        payloads.forEach(p -> ByteBuffers.copyTo(p, writer.newDataAtom(p.remaining())));
    }

    private BaggageReader makeBaggage() {
        BaggageWriter writer = BaggageWriter.create();
        writePayloads(writer, rootPayload);
        writer.enter(first);
        writePayloads(writer, firstPayload);
        writer.enter(second);
        writePayloads(writer, secondPayload);
        writer.enter(third);
        writePayloads(writer, thirdPayload);
        writer.exit();
        return BaggageReader.create(writer.atoms());
    }

    @Test
    public void testEnter() {
        BaggageReader reader = makeBaggage();
        assertTrue(reader.enter(first));
        assertTrue(reader.enter(second));
        assertTrue(reader.enter(third));
        assertNull(reader.enter());

        reader = makeBaggage();
        assertEquals(first, reader.enter());
        assertEquals(second, reader.enter());
        assertEquals(third, reader.enter());
        assertNull(reader.enter());

        reader = makeBaggage();
        assertFalse(reader.enter(second));
        assertFalse(reader.hasNext());

        reader = makeBaggage();
        assertFalse(reader.enter(third));
        assertFalse(reader.hasNext());
    }

    @Test
    public void testNextDataExplicitEntry() {
        BaggageReader reader = makeBaggage();

        Consumer<ByteBuffer> checkNext = p -> assertEquals(p, reader.nextData());

        rootPayload.forEach(checkNext);
        assertNull(reader.nextData());

        reader.enter(first);
        firstPayload.forEach(checkNext);
        assertNull(reader.nextData());

        reader.enter(second);
        secondPayload.forEach(checkNext);
        assertNull(reader.nextData());

        reader.enter(third);
        thirdPayload.forEach(checkNext);
        assertNull(reader.nextData());

        assertNull(reader.nextData());
        reader.exit();
        assertNull(reader.nextData());
        reader.exit();
        assertNull(reader.nextData());
        reader.exit();
        assertNull(reader.nextData());
    }

    @Test
    public void testNextDataImplicitEntry() {
        BaggageReader reader = makeBaggage();

        Consumer<ByteBuffer> checkNext = p -> assertEquals(p, reader.nextData());

        rootPayload.forEach(checkNext);
        assertNull(reader.nextData());

        reader.enter();
        firstPayload.forEach(checkNext);
        assertNull(reader.nextData());

        reader.enter();
        secondPayload.forEach(checkNext);
        assertNull(reader.nextData());

        reader.enter();
        thirdPayload.forEach(checkNext);
        assertNull(reader.nextData());

        assertNull(reader.nextData());
        reader.exit();
        assertNull(reader.nextData());
        reader.exit();
        assertNull(reader.nextData());
        reader.exit();
        assertNull(reader.nextData());
    }

    @Test
    public void testHasNext() {
        BaggageReader reader = makeBaggage();

        Consumer<ByteBuffer> checkNext = p -> {
            assertTrue(reader.hasNext());
            reader.nextData();
        };

        rootPayload.forEach(checkNext);
        assertTrue(reader.hasNext());

        reader.enter();
        firstPayload.forEach(checkNext);
        assertTrue(reader.hasNext());

        reader.enter();
        secondPayload.forEach(checkNext);
        assertTrue(reader.hasNext());

        reader.enter();
        thirdPayload.forEach(checkNext);

        assertFalse(reader.hasNext());
        reader.exit();
        assertFalse(reader.hasNext());
        reader.exit();
        assertFalse(reader.hasNext());
        reader.exit();
        assertFalse(reader.hasNext());
    }

    @Test
    public void testHasData() {
        BaggageReader reader = makeBaggage();

        Consumer<ByteBuffer> checkNext = p -> {
            assertTrue(reader.hasData());
            reader.nextData();
        };

        rootPayload.forEach(checkNext);
        assertFalse(reader.hasData());

        reader.enter();
        firstPayload.forEach(checkNext);
        assertFalse(reader.hasData());

        reader.enter();
        secondPayload.forEach(checkNext);
        assertFalse(reader.hasData());

        reader.enter();
        thirdPayload.forEach(checkNext);

        assertFalse(reader.hasData());
        reader.exit();
        assertFalse(reader.hasData());
        reader.exit();
        assertFalse(reader.hasData());
        reader.exit();
        assertFalse(reader.hasData());
    }

    @Test
    public void testHasChild() {
        BaggageReader reader = makeBaggage();

        Consumer<ByteBuffer> checkNext = p -> {
            assertFalse(reader.hasChild());
            reader.nextData();
        };

        rootPayload.forEach(checkNext);
        assertTrue(reader.hasChild());

        reader.enter();
        firstPayload.forEach(checkNext);
        assertTrue(reader.hasChild());

        reader.enter();
        secondPayload.forEach(checkNext);
        assertTrue(reader.hasChild());

        reader.enter();
        thirdPayload.forEach(checkNext);

        assertFalse(reader.hasChild());
        reader.exit();
        assertFalse(reader.hasChild());
        reader.exit();
        assertFalse(reader.hasChild());
        reader.exit();
        assertFalse(reader.hasChild());
    }

    @Test
    public void testEnterEarlierBagExplicit() {
        BaggageReader reader = makeBaggage();

        assertFalse(reader.enter(indexed(first.index - 1)));
        assertTrue(reader.enter(first));
        assertFalse(reader.enter(indexed(second.index - 1)));
        assertTrue(reader.enter(second));
        assertFalse(reader.enter(indexed(third.index - 1)));
        assertTrue(reader.enter(third));
    }

    @Test
    public void testEnterEarlierBagImplicit() {
        BaggageReader reader = makeBaggage();

        assertFalse(reader.enter(indexed(first.index - 1)));
        assertEquals(first, reader.enter());
        assertFalse(reader.enter(indexed(second.index - 1)));
        assertEquals(second, reader.enter());
        assertFalse(reader.enter(indexed(third.index - 1)));
        assertEquals(third, reader.enter());
    }

    @Test
    public void testEnterLaterBagExplicit() {
        BaggageReader reader = makeBaggage();
        reader.enter();
        reader.enter();
        assertFalse(reader.enter(indexed(third.index + 1)));
        assertFalse(reader.enter(third));

        reader = makeBaggage();
        reader.enter();
        assertFalse(reader.enter(indexed(second.index + 1)));
        assertFalse(reader.enter(second));

        reader = makeBaggage();
        assertFalse(reader.enter(indexed(first.index + 1)));
        assertFalse(reader.enter(first));
    }

    @Test
    public void testEnterLaterBagImplicit() {
        BaggageReader reader = makeBaggage();
        reader.enter();
        reader.enter();
        assertFalse(reader.enter(indexed(third.index + 1)));
        assertNull(reader.enter());

        reader = makeBaggage();
        reader.enter();
        assertFalse(reader.enter(indexed(second.index + 1)));
        assertNull(reader.enter());

        reader = makeBaggage();
        assertFalse(reader.enter(indexed(first.index + 1)));
        assertNull(reader.enter());
    }

    @Test
    public void testEnterSkipsChildData() {
        BaggageReader reader = makeBaggage();

        assertTrue(reader.hasData());
        reader.enter(indexed(first.index - 1));
        assertFalse(reader.hasData());
        reader.enter(first);

        assertTrue(reader.hasData());
        reader.enter(indexed(second.index - 1));
        assertFalse(reader.hasData());
        reader.enter(second);

        assertTrue(reader.hasData());
        reader.enter(indexed(third.index - 1));
        assertFalse(reader.hasData());
        reader.enter(third);

        assertTrue(reader.hasData());
        reader.enter();
        assertFalse(reader.hasData());
    }

    @Test
    public void testFinishClearsBag() {
        BaggageReader reader = makeBaggage();
        assertTrue(reader.hasNext());
        reader.finish();
        assertFalse(reader.hasNext());
    }

    @Test
    public void testExitThrowsException() {
        BaggageReader reader = makeBaggage();
        reader.enter();
        reader.enter();
        reader.enter();
        reader.exit();
        reader.exit();
        reader.exit();
        exception.expect(BaggageLayerRuntimeException.class);
        reader.exit();
    }

    @Test
    public void testFinishMarksAllAtomsUnprocessed() {
        BaggageReader reader = makeBaggage();
        reader.finish();
        assertEquals(Lists.newArrayList(allAtoms), reader.unprocessedAtoms());

        reader = makeBaggage();
        reader.enter();
        reader.finish();
        assertEquals(Lists.newArrayList(allAtoms), reader.unprocessedAtoms());

        reader = makeBaggage();
        reader.enter();
        reader.enter();
        reader.finish();
        assertEquals(Lists.newArrayList(allAtoms), reader.unprocessedAtoms());

        reader = makeBaggage();
        reader.enter();
        reader.enter();
        reader.enter();
        reader.finish();
        assertEquals(Lists.newArrayList(allAtoms), reader.unprocessedAtoms());
    }

    @Test
    public void testEnterLaterBagMarksAllAtomsUnprocessed() {
        BaggageReader reader = makeBaggage();
        reader.enter(indexed(first.index + 1));
        reader.finish();
        assertEquals(Lists.newArrayList(allAtoms), reader.unprocessedAtoms());
    }

    @Test
    public void testPartiallyProcessedDataIsNotMarkedAsUnprocessed1() {

        ArrayList<ByteBuffer> expect = Lists.newArrayList(headerAtom(first, 0), headerAtom(second, 1),
                                                          headerAtom(third, 2), dataAtom(thirdPayload.get(0)));

        BaggageReader reader = makeBaggage();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.enter();
        reader.finish();
        assertEquals(expect, reader.unprocessedAtoms());

        reader = makeBaggage();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.enter();
        reader.exit();
        reader.finish();
        assertEquals(expect, reader.unprocessedAtoms());

        reader = makeBaggage();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.enter();
        reader.exit();
        reader.exit();
        reader.finish();
        assertEquals(expect, reader.unprocessedAtoms());

        reader = makeBaggage();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.enter();
        reader.exit();
        reader.exit();
        reader.exit();
        reader.finish();
        assertEquals(expect, reader.unprocessedAtoms());

    }

    @Test
    public void testPartiallyProcessedDataIsNotMarkedAsUnprocessed2() {
        ArrayList<ByteBuffer> expect = Lists.newArrayList(dataAtom(rootPayload.get(0)), headerAtom(first, 0),
                                                          dataAtom(firstPayload.get(0)), dataAtom(firstPayload.get(1)));

        BaggageReader reader = makeBaggage();
        reader.enter();
        reader.enter();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.finish();
        assertEquals(expect, reader.unprocessedAtoms());

        reader = makeBaggage();
        reader.enter();
        reader.enter();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.exit();
        reader.finish();
        assertEquals(expect, reader.unprocessedAtoms());

        reader = makeBaggage();
        reader.enter();
        reader.enter();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.exit();
        reader.exit();
        reader.finish();
        assertEquals(expect, reader.unprocessedAtoms());

        reader = makeBaggage();
        reader.enter();
        reader.enter();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.exit();
        reader.exit();
        reader.exit();
        reader.finish();
        assertEquals(expect, reader.unprocessedAtoms());
    }

    @Test
    public void testPartiallyProcessedDataIsNotMarkedAsUnprocessed3() {
        ArrayList<ByteBuffer> expect =
                Lists.newArrayList(headerAtom(first, 0), headerAtom(second, 1), dataAtom(secondPayload.get(0)),
                                   dataAtom(secondPayload.get(1)), dataAtom(secondPayload.get(2)));

        BaggageReader reader = makeBaggage();
        reader.dropData();
        reader.enter();
        reader.dropData();
        reader.enter();
        reader.enter();
        reader.dropData();
        reader.finish();
        assertEquals(expect, reader.unprocessedAtoms());

        reader = makeBaggage();
        reader.dropData();
        reader.enter();
        reader.dropData();
        reader.enter();
        reader.enter();
        reader.dropData();
        reader.exit();
        reader.finish();
        assertEquals(expect, reader.unprocessedAtoms());

        reader = makeBaggage();
        reader.dropData();
        reader.enter();
        reader.dropData();
        reader.enter();
        reader.enter();
        reader.dropData();
        reader.exit();
        reader.exit();
        reader.finish();
        assertEquals(expect, reader.unprocessedAtoms());

        reader = makeBaggage();
        reader.dropData();
        reader.enter();
        reader.dropData();
        reader.enter();
        reader.enter();
        reader.dropData();
        reader.exit();
        reader.exit();
        reader.exit();
        reader.finish();
        assertEquals(expect, reader.unprocessedAtoms());
    }

    @Test
    public void testPartiallyProcessedDataIsNotMarkedAsUnprocessed4() {
        ArrayList<ByteBuffer> expect = Lists.newArrayList(headerAtom(first, 0), headerAtom(second, 1),
                                                          headerAtom(third, 2), dataAtom(thirdPayload.get(0)));

        BaggageReader reader = makeBaggage();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.enter();
        reader.nextData();
        assertEquals(expect, reader.unprocessedAtoms());

        reader = makeBaggage();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.finish();
        assertEquals(expect, reader.unprocessedAtoms());

        reader = makeBaggage();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.exit();
        reader.finish();
        assertEquals(expect, reader.unprocessedAtoms());

        reader = makeBaggage();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.enter();
        reader.nextData();
        reader.exit();
        reader.exit();
        reader.finish();
        assertEquals(expect, reader.unprocessedAtoms());
    }

    @Test
    public void testDidNotOverflow() {
        BaggageReader reader = makeBaggage();

        assertFalse(reader.didOverflow());
        reader.finish();
        assertFalse(reader.didOverflow());
        assertNull(reader.overflowAtoms());
    }
}
