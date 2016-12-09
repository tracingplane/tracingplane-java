package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

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
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;

/**
 * Tests a baggage containing a bag that contains a bag that contains ...
 */
public class TestBaggage06_Siblings extends BaggageTestCase {

    private final BagKey.Indexed A = indexed(4);
    private final BagKey.Indexed B = indexed(30);
    private final BagKey.Indexed C = indexed(-10);

    private final BagKey.Indexed AA = indexed(9);
    private final BagKey.Indexed AB = indexed(13);

    private final BagKey.Indexed AAA = indexed(8282);

    private final BagKey.Indexed BA = indexed(342);
    private final BagKey.Indexed BB = indexed(4430);
    private final BagKey.Indexed BC = indexed(-393932);

    private final List<ByteBuffer> payloadAAA = Lists.newArrayList(randomBytes(100));
    private final List<ByteBuffer> payloadBA = Lists.newArrayList(randomBytes(1222), randomBytes(1234));
    private final List<ByteBuffer> payloadBB = Lists.newArrayList(randomBytes(3), randomBytes(1), randomBytes(0));
    private final List<ByteBuffer> payloadBC = Lists.newArrayList(randomBytes(5));
    private final List<ByteBuffer> payloadC = Lists.newArrayList(randomBytes(5));

    private final List<ByteBuffer> allAtoms =
            Lists.newArrayList(headerAtom(A, 0), headerAtom(AA, 1), headerAtom(AAA, 2), dataAtom(payloadAAA.get(0)),
                               headerAtom(B, 0), headerAtom(BA, 1), dataAtom(payloadBA.get(0)),
                               dataAtom(payloadBA.get(1)), headerAtom(BB, 1), dataAtom(payloadBB.get(0)),
                               dataAtom(payloadBB.get(1)), dataAtom(payloadBB.get(2)), headerAtom(BC, 1),
                               dataAtom(payloadBC.get(0)), headerAtom(C, 0), dataAtom(payloadC.get(0)));

    private void writePayloads(BaggageWriter writer, List<ByteBuffer> payloads) {
        payloads.forEach(p -> ByteBuffers.copyTo(p, writer.newDataAtom(p.remaining())));
    }

    private BaggageReader makeBaggage() {
        BaggageWriter writer = BaggageWriter.create();

        {
            writer.enter(A);
            {
                writer.enter(AA).enter(AAA);
                writePayloads(writer, payloadAAA);
                writer.exit().exit();
            }
            {
                writer.enter(AB);
                writer.exit();
            }
            writer.exit();
        }
        {
            writer.enter(B);
            {
                writer.enter(BA);
                writePayloads(writer, payloadBA);
                writer.exit();
            }
            {
                writer.enter(BB);
                writePayloads(writer, payloadBB);
                writer.exit();
            }
            {
                writer.enter(BC);
                writePayloads(writer, payloadBC);
                writer.exit();
            }
            writer.exit();
        }
        {
            writer.enter(C);
            writePayloads(writer, payloadC);
            writer.exit();
        }

        return BaggageReader.create(writer.atoms());
    }

    @Test
    public void testEnter() {
        BaggageReader reader = makeBaggage();
        assertTrue(reader.enter(A));
        assertTrue(reader.enter(AA));
        assertTrue(reader.enter(AAA));
        assertNull(reader.enter());
        reader.exit();
        assertNull(reader.enter());
        reader.exit();
        assertTrue(reader.enter(AB));
        assertNull(reader.enter());
        reader.exit();
        assertNull(reader.enter());
        reader.exit();
        assertTrue(reader.enter(B));
        assertTrue(reader.enter(BA));
        assertNull(reader.enter());
        reader.exit();
        assertTrue(reader.enter(BB));
        assertNull(reader.enter());
        reader.exit();
        assertTrue(reader.enter(BC));
        assertNull(reader.enter());
        reader.exit();
        assertNull(reader.enter());
        reader.exit();
        assertTrue(reader.enter(C));
        assertNull(reader.enter());
        reader.exit();
        assertNull(reader.enter());
    }

    @Test
    public void testNextDataExplicitEntry() {
        BaggageReader reader = makeBaggage();

        Consumer<ByteBuffer> checkNext = p -> assertEquals(p, reader.nextData());

        assertNull(reader.nextData());
        reader.enter(A);
        assertNull(reader.nextData());
        reader.enter(AA);
        assertNull(reader.nextData());
        reader.enter(AAA);
        payloadAAA.forEach(checkNext);
        assertNull(reader.nextData());

        reader.exit();
        assertNull(reader.nextData());
        reader.exit();
        assertNull(reader.nextData());
        reader.exit();
        assertNull(reader.nextData());

        reader.enter(B);
        assertNull(reader.nextData());
        reader.enter(BA);
        payloadBA.forEach(checkNext);
        assertNull(reader.nextData());

        reader.exit();
        assertNull(reader.nextData());
        reader.enter(BB);
        payloadBB.forEach(checkNext);
        assertNull(reader.nextData());

        reader.exit();
        assertNull(reader.nextData());
        reader.enter(BC);
        payloadBC.forEach(checkNext);
        assertNull(reader.nextData());

        reader.exit();
        assertNull(reader.nextData());
        reader.exit();
        assertNull(reader.nextData());
        reader.enter(C);
        payloadC.forEach(checkNext);
        assertNull(reader.nextData());
        assertNull(reader.nextData());

        reader.exit();
        assertNull(reader.nextData());
    }

    @Test
    public void testReadOneChildBagOnly() {
        BaggageReader reader = makeBaggage();

        Consumer<ByteBuffer> checkNext = p -> assertEquals(p, reader.nextData());

        assertTrue(reader.enter(B));
        assertTrue(reader.enter(BC));
        payloadBC.forEach(checkNext);
    }

    @Test
    public void testReadOneChildBagOnly2() {
        BaggageReader reader = makeBaggage();

        Consumer<ByteBuffer> checkNext = p -> assertEquals(p, reader.nextData());

        assertTrue(reader.enter(C));
        payloadC.forEach(checkNext);
    }

    @Test
    public void testEnterLaterBag1() {
        BaggageReader reader = makeBaggage();

        assertTrue(reader.enter(A));
        assertFalse(reader.enter(indexed(AA.index + 1)));
        assertTrue(reader.enter(AB));
        reader.exit();
        reader.exit();
        assertFalse(reader.enter(indexed(A.index + 1)));
        assertTrue(reader.enter(B));
    }

    @Test
    public void testEnterLaterBag2() {
        BaggageReader reader = makeBaggage();

        assertFalse(reader.enter(indexed(A.index + 1)));
        assertFalse(reader.enter(A));
        assertTrue(reader.enter(B));
    }

    @Test
    public void testEnterLaterBag3() {
        BaggageReader reader = makeBaggage();

        assertFalse(reader.enter(indexed(A.index + 1)));
        assertFalse(reader.enter(A));
        assertFalse(reader.enter(indexed(B.index + 1)));
        assertFalse(reader.enter(B));
        assertTrue(reader.enter(C));
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

        reader = makeBaggage();
        reader.enter();
        reader.exit();
        reader.enter();
        reader.exit();
        reader.enter();
        reader.exit();
        reader.finish();
        assertEquals(Lists.newArrayList(allAtoms), reader.unprocessedAtoms());

        reader = makeBaggage();
        reader.enter();
        reader.enter();
        reader.exit();
        reader.exit();
        reader.finish();
        assertEquals(Lists.newArrayList(allAtoms), reader.unprocessedAtoms());
    }

    @Test
    public void testEnterLaterBagMarksAllAtomsUnprocessed() {
        BaggageReader reader = makeBaggage();
        reader.enter(C);
        reader.finish();
        assertEquals(Lists.newArrayList(allAtoms), reader.unprocessedAtoms());
    }

    @Test
    public void testPartiallyProcessedDataIsNotMarkedAsUnprocessed1() {

        ArrayList<ByteBuffer> expect =
                Lists.newArrayList(headerAtom(A, 0), headerAtom(AA, 1), headerAtom(AAA, 2), dataAtom(payloadAAA.get(0)),
                                   headerAtom(C, 0), dataAtom(payloadC.get(0)));

        BaggageReader reader = makeBaggage();

        Consumer<ByteBuffer> readAll = p -> reader.nextData();

        {
            reader.enter(B);
            {
                reader.enter(BA);
                payloadBA.forEach(readAll);
                reader.exit();
            }
            {
                reader.enter(BB);
                payloadBB.forEach(readAll);
                reader.exit();
            }
            {
                reader.enter(BC);
                payloadBC.forEach(readAll);
                reader.exit();
            }
            reader.exit();
        }
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
