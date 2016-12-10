package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import com.google.common.collect.Lists;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;

public class TestReassembleUnprocessed extends BaggageTestCase {

    BagKey a = indexed(3);
    List<ByteBuffer> payloadA = Lists.newArrayList(randomBytes(838), randomBytes(712));

    BagKey aa = indexed(0);
    List<ByteBuffer> payloadAA = Lists.newArrayList(randomBytes(330));

    BagKey b = indexed(10);
    List<ByteBuffer> payloadB = Lists.newArrayList(randomBytes(443), randomBytes(337));

    private static void writePayload(BaggageWriter writer, List<ByteBuffer> payload) {
        payload.forEach(p -> ByteBuffers.copyTo(p, writer.newDataAtom(p.remaining())));
    }

    private static void readPayload(BaggageReader reader, List<ByteBuffer> expectedPayload) {
        expectedPayload.forEach(p -> assertEquals(p, reader.nextData()));
    }

    private void maybeEnter(BagKey key, boolean shouldEnter) {
        if (shouldEnter) writer.enter(key);
    }

    private void maybeWritePayload(List<ByteBuffer> payload, boolean shouldWritePayload) {
        if (shouldWritePayload) writePayload(writer, payload);
    }

    private void maybeExit(boolean shouldExit) {
        if (shouldExit) writer.exit();
    }

    private BaggageWriter writer;

    @Before
    public void resetWriter() {
        writer = BaggageWriter.create();
    }
    
    public void resetWriter(Iterator<ByteBuffer> withIterator) {
        writer = BaggageWriter.createAndMergeWith(withIterator);
    }
    
    public void resetWriter(Iterator<ByteBuffer> withIteratorA, Iterator<ByteBuffer> withIteratorB) {
        writer = BaggageWriter.createAndMergeWith(withIteratorA, withIteratorB);
    }

    private List<ByteBuffer> writeAtoms(boolean addA, boolean addAA, boolean addB) {
        {
            maybeEnter(a, addA || addAA);
            maybeWritePayload(payloadA, addA);
            {
                maybeEnter(aa, addAA);
                maybeWritePayload(payloadAA, addAA);
                maybeExit(addAA);
            }
            maybeExit(addA || addAA);
        }
        {
            maybeEnter(b, addB);
            maybeWritePayload(payloadB, addB);
            maybeExit(addB);
        }

        return writer.atoms();
    }

    @Test
    public void testReassembleTail() {
        List<ByteBuffer> allAtoms = writeAtoms(true, true, true);
        BaggageReader reader = BaggageReader.create(allAtoms);

        {
            reader.enter(a);
            readPayload(reader, payloadA);
            {
                reader.enter(aa);
                readPayload(reader, payloadAA);
                reader.exit();
            }
            reader.exit();
        }

        List<ByteBuffer> unprocessed = reader.unprocessedAtoms();
        List<ByteBuffer> overflow = reader.overflowAtoms();

        assertNotNull(unprocessed);
        assertNull(overflow);
        
        resetWriter();
        List<ByteBuffer> onlyB = writeAtoms(false, false, true);
        assertEquals(onlyB, unprocessed);
        
        resetWriter(onlyB.iterator());
        List<ByteBuffer> atoms = writeAtoms(true, true, false);
        
        resetWriter();
        List<ByteBuffer> expect = writeAtoms(true, true, true);
        assertEquals(expect.size(), atoms.size());
        assertEquals(expect, atoms);

    }

    @Test
    public void testReassembleHead() {
        List<ByteBuffer> allAtoms = writeAtoms(true, true, true);
        BaggageReader reader = BaggageReader.create(allAtoms);

        {
            reader.enter(b);
            readPayload(reader, payloadB);
            reader.exit();
        }

        List<ByteBuffer> unprocessed = reader.unprocessedAtoms();
        List<ByteBuffer> overflow = reader.overflowAtoms();

        assertNotNull(unprocessed);
        assertNull(overflow);
        
        resetWriter();
        List<ByteBuffer> A_and_AA = writeAtoms(true, true, false);
        assertEquals(A_and_AA, unprocessed);
        
        resetWriter(A_and_AA.iterator());
        List<ByteBuffer> atoms = writeAtoms(false, false, true);
        
        resetWriter();
        List<ByteBuffer> expect = writeAtoms(true, true, true);
        assertEquals(expect.size(), atoms.size());
        assertEquals(expect, atoms);

    }

    @Test
    public void testReassembleMiddle() {
        List<ByteBuffer> allAtoms = writeAtoms(true, true, true);
        BaggageReader reader = BaggageReader.create(allAtoms);

        {
            reader.enter(a);
            readPayload(reader, payloadA);
            reader.exit();
            reader.enter(b);
            readPayload(reader, payloadB);
            reader.exit();
        }

        List<ByteBuffer> unprocessed = reader.unprocessedAtoms();
        List<ByteBuffer> overflow = reader.overflowAtoms();

        assertNotNull(unprocessed);
        assertNull(overflow);
        
        resetWriter();
        List<ByteBuffer> onlyAA = writeAtoms(false, true, false);
        assertEquals(onlyAA, unprocessed);
        
        resetWriter(onlyAA.iterator());
        List<ByteBuffer> atoms = writeAtoms(true, false, true);
        
        resetWriter();
        List<ByteBuffer> expect = writeAtoms(true, true, true);
        assertEquals(expect.size(), atoms.size());
        assertEquals(expect, atoms);
    }

    @Test
    public void testReassembleMiddle2() {
        List<ByteBuffer> allAtoms = writeAtoms(true, true, true);
        BaggageReader reader = BaggageReader.create(allAtoms);

        {
            reader.enter(a);
            reader.enter(aa);
            readPayload(reader, payloadAA);
            reader.exit();
            reader.exit();
            reader.enter(b);
            readPayload(reader, payloadB);
            reader.exit();
        }

        List<ByteBuffer> unprocessed = reader.unprocessedAtoms();
        List<ByteBuffer> overflow = reader.overflowAtoms();

        assertNotNull(unprocessed);
        assertNull(overflow);
        
        resetWriter();
        List<ByteBuffer> onlyA = writeAtoms(true, false, false);
        assertEquals(onlyA, unprocessed);
        
        resetWriter(onlyA.iterator());
        List<ByteBuffer> atoms = writeAtoms(false, true, true);
        
        resetWriter();
        List<ByteBuffer> expect = writeAtoms(true, true, true);
        assertEquals(expect.size(), atoms.size());
        assertEquals(expect, atoms);
    }

    @Test
    public void testReassembleMiddle2AndOverflow() {
        List<ByteBuffer> allAtoms = writeAtoms(true, true, true);
        allAtoms.add(1, BaggageAtoms.OVERFLOW_MARKER);
        BaggageReader reader = BaggageReader.create(allAtoms);

        {
            reader.enter(a);
            reader.enter(aa);
            readPayload(reader, payloadAA);
            reader.exit();
            reader.exit();
            reader.enter(b);
            readPayload(reader, payloadB);
            reader.exit();
        }

        List<ByteBuffer> unprocessed = reader.unprocessedAtoms();
        List<ByteBuffer> overflow = reader.overflowAtoms();

        assertNotNull(unprocessed);
        assertNotNull(overflow);
        
        resetWriter();
        List<ByteBuffer> onlyA = writeAtoms(true, false, false);
        assertEquals(onlyA, unprocessed);
        
        assertEquals(Lists.newArrayList(allAtoms.get(0), BaggageAtoms.OVERFLOW_MARKER), overflow);
        
        resetWriter(onlyA.iterator(), overflow.iterator());
        List<ByteBuffer> atoms = writeAtoms(false, true, true);
        
        resetWriter();
        List<ByteBuffer> expect = writeAtoms(true, true, true);
        expect.add(1, BaggageAtoms.OVERFLOW_MARKER);
        assertEquals(expect.size(), atoms.size());
        assertEquals(expect, atoms);
    }
}
