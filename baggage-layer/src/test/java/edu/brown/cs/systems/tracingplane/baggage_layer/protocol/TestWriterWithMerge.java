package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.google.common.collect.Lists;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;

public class TestWriterWithMerge extends BaggageTestCase {

    private final BagKey.Indexed first = indexed(4);
    private final BagKey.Indexed second = indexed(10);
    private final BagKey.Indexed third = indexed(25);
    private final BagKey.Indexed fourth = indexed(9);

    private final List<ByteBuffer> payloadA = Lists.newArrayList(randomBytes(88), randomBytes(3));
    private final List<ByteBuffer> payloadB = Lists.newArrayList(randomBytes(3), randomBytes(7));
    private final List<ByteBuffer> payloadC = Lists.newArrayList(randomBytes(13));

    private final List<ByteBuffer> allAtoms =
            Lists.newArrayList(headerAtom(first, 0), headerAtom(second, 1), dataAtom(payloadA.get(0)),
                               dataAtom(payloadA.get(1)), headerAtom(third, 1), dataAtom(payloadB.get(0)),
                               dataAtom(payloadB.get(1)), headerAtom(fourth, 0), dataAtom(payloadC.get(0)));

    private List<ByteBuffer> makeAtoms() {
        BaggageWriter writer = BaggageWriter.create();
        writer.enter(first);
        writer.enter(second);
        payloadA.forEach(p -> ByteBuffers.copyTo(p, writer.newDataAtom(p.remaining())));
        writer.exit();
        writer.enter(third);
        payloadB.forEach(p -> ByteBuffers.copyTo(p, writer.newDataAtom(p.remaining())));
        writer.exit();
        writer.exit();
        writer.enter(fourth);
        payloadC.forEach(p -> ByteBuffers.copyTo(p, writer.newDataAtom(p.remaining())));
        writer.exit();
        return writer.atoms();
    }

    @Test
    public void testBaggageCorrect() {
        BaggageReader reader = BaggageReader.create(makeAtoms());

        assertTrue(reader.enter(first));
        assertTrue(reader.enter(second));
        payloadA.forEach(p -> assertEquals(p, reader.nextData()));
        reader.exit();
        assertTrue(reader.enter(third));
        payloadB.forEach(p -> assertEquals(p, reader.nextData()));
        reader.exit();
        reader.exit();
        assertTrue(reader.enter(fourth));
        payloadC.forEach(p -> assertEquals(p, reader.nextData()));
        reader.exit();
    }

    private BaggageReader makeAndGetOverflow(List<ByteBuffer> atoms, int insertionIndex) {
        List<ByteBuffer> atomsWithOverflow = new ArrayList<>(atoms.size() + 1);
        atomsWithOverflow.addAll(atoms);
        atomsWithOverflow.add(insertionIndex, BaggageAtoms.OVERFLOW_MARKER);
        BaggageReader reader = BaggageReader.create(atomsWithOverflow);
        reader.finish();
        return reader;
    }

    @Test
    public void testOverflowLocation0() {
        List<ByteBuffer> atoms = makeAtoms();

        BaggageReader reader;

        reader = makeAndGetOverflow(atoms, 0);
        assertEquals(allAtoms, reader.unprocessedAtoms());
        assertTrue(reader.didOverflow());
        assertOverflowPath(reader.overflowAtoms());
    }

    @Test
    public void testOverflowLocation1() {
        List<ByteBuffer> atoms = makeAtoms();

        BaggageReader reader;

        reader = makeAndGetOverflow(atoms, 1);
        assertEquals(allAtoms, reader.unprocessedAtoms());
        assertTrue(reader.didOverflow());
        assertOverflowPath(reader.overflowAtoms(), first);
    }

    @Test
    public void testOverflowLocation2() {
        List<ByteBuffer> atoms = makeAtoms();

        BaggageReader reader;

        reader = makeAndGetOverflow(atoms, 2);
        assertEquals(allAtoms, reader.unprocessedAtoms());
        assertTrue(reader.didOverflow());
        assertOverflowPath(reader.overflowAtoms(), first, second);
    }

    @Test
    public void testOverflowLocation3() {
        List<ByteBuffer> atoms = makeAtoms();

        BaggageReader reader;

        reader = makeAndGetOverflow(atoms, 3);
        assertEquals(allAtoms, reader.unprocessedAtoms());
        assertTrue(reader.didOverflow());
        assertOverflowPath(reader.overflowAtoms(), first, second);
    }

    @Test
    public void testOverflowLocation4() {
        List<ByteBuffer> atoms = makeAtoms();

        BaggageReader reader;

        reader = makeAndGetOverflow(atoms, 4);
        assertEquals(allAtoms, reader.unprocessedAtoms());
        assertTrue(reader.didOverflow());
        assertOverflowPath(reader.overflowAtoms(), first, second);
    }

    @Test
    public void testOverflowLocation5() {
        List<ByteBuffer> atoms = makeAtoms();

        BaggageReader reader;

        reader = makeAndGetOverflow(atoms, 5);
        assertEquals(allAtoms, reader.unprocessedAtoms());
        assertTrue(reader.didOverflow());
        assertOverflowPath(reader.overflowAtoms(), first, third);
    }

    @Test
    public void testOverflowLocation6() {
        List<ByteBuffer> atoms = makeAtoms();

        BaggageReader reader;

        reader = makeAndGetOverflow(atoms, 6);
        assertEquals(allAtoms, reader.unprocessedAtoms());
        assertTrue(reader.didOverflow());
        assertOverflowPath(reader.overflowAtoms(), first, third);
    }

    @Test
    public void testOverflowLocation7() {
        List<ByteBuffer> atoms = makeAtoms();

        BaggageReader reader;

        reader = makeAndGetOverflow(atoms, 7);
        assertEquals(allAtoms, reader.unprocessedAtoms());
        assertTrue(reader.didOverflow());
        assertOverflowPath(reader.overflowAtoms(), first, third);
    }

    @Test
    public void testOverflowLocation8() {
        List<ByteBuffer> atoms = makeAtoms();

        BaggageReader reader;

        reader = makeAndGetOverflow(atoms, 8);
        assertEquals(allAtoms, reader.unprocessedAtoms());
        assertTrue(reader.didOverflow());
        assertOverflowPath(reader.overflowAtoms(), fourth);
    }

    @Test
    public void testOverflowLocation9() {
        List<ByteBuffer> atoms = makeAtoms();

        BaggageReader reader;

        reader = makeAndGetOverflow(atoms, 9);
        assertEquals(allAtoms, reader.unprocessedAtoms());
        assertTrue(reader.didOverflow());
        assertOverflowPath(reader.overflowAtoms(), fourth);
    }

}
