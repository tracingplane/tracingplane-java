package brown.tracingplane.baggageprotocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.google.common.collect.Lists;
import brown.tracingplane.atomlayer.ByteBuffers;
import brown.tracingplane.atomlayer.Lexicographic;

public class TestOverflowMarkerMerge extends BaggageTestCase {

    private final BagKey.Indexed first = indexed(4);
    private final BagKey.Indexed second = indexed(10);
    private final BagKey.Indexed third = indexed(25);
    private final BagKey.Indexed fourth = indexed(9);

    private final List<ByteBuffer> payloadA = Lists.newArrayList(randomBytes(88), randomBytes(3));
    private final List<ByteBuffer> payloadB = Lists.newArrayList(randomBytes(3), randomBytes(7));
    private final List<ByteBuffer> payloadC = Lists.newArrayList(randomBytes(13));

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

    private List<ByteBuffer> addOverflowMarker(List<ByteBuffer> atoms, int insertionIndex) {
        List<ByteBuffer> atomsWithOverflow = new ArrayList<>(atoms.size() + 1);
        atomsWithOverflow.addAll(atoms);
        atomsWithOverflow.add(insertionIndex, BaggageProtocol.OVERFLOW_MARKER);
        return atomsWithOverflow;
    }

    private void testLocation(int insertionIndex, int expectedIndexInMerged) {
        List<ByteBuffer> atoms = makeAtoms();
        List<ByteBuffer> atomsWithOverflow = addOverflowMarker(atoms, insertionIndex);

        BaggageReader reader = BaggageReader.create(atomsWithOverflow);

        assertNotEquals(atomsWithOverflow, atoms);
        assertEquals(atoms, reader.unprocessedAtoms());

        List<ByteBuffer> expectedMergedAtoms = addOverflowMarker(atoms, expectedIndexInMerged);
        assertEquals(expectedMergedAtoms, Lexicographic.merge(atoms, reader.overflowAtoms()));
        assertEquals(expectedMergedAtoms, Lexicographic.merge(reader.unprocessedAtoms(), reader.overflowAtoms()));
    }

    @Test
    public void testOverflowLocation0() {
        testLocation(0, 0);
    }

    @Test
    public void testOverflowLocation1() {
        testLocation(1, 1);
    }

    @Test
    public void testOverflowLocation2() {
        testLocation(2, 2);
    }

    @Test
    public void testOverflowLocation3() {
        testLocation(3, 2);
    }

    @Test
    public void testOverflowLocation4() {
        testLocation(4, 2);
    }

    @Test
    public void testOverflowLocation5() {
        testLocation(5, 5);
    }

    @Test
    public void testOverflowLocation6() {
        testLocation(6, 5);
    }

    @Test
    public void testOverflowLocation7() {
        testLocation(7, 5);
    }

    @Test
    public void testOverflowLocation8() {
        testLocation(8, 8);
    }

    @Test
    public void testOverflowLocation9() {
        testLocation(9, 8);
    }

}
