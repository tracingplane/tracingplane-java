package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import com.google.common.collect.Lists;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.atom_layer.types.AtomLayerException;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayerException;

public class TestOverflowMarker {

    private static final Random r = new Random(0);

    private static ByteBuffer randomBytes(int length) {
        byte[] bytes = new byte[length];
        r.nextBytes(bytes);
        return ByteBuffer.wrap(bytes);
    }

    private static BagKey.Indexed indexed(int index) {
        return (BagKey.Indexed) BagKey.indexed(index);
    }

    private static List<ByteBuffer> writeBag(ByteBuffer... contents) {
        BaggageWriter writer = BaggageWriter.create();
        for (ByteBuffer content : contents) {
            ByteBuffers.copyTo(content, writer.newDataAtom(content.remaining()));
        }
        return writer.atoms();
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

    private void assertOverflowPath(List<ByteBuffer> overflowAtoms, BagKey... path) {
        assertOverflowPath(overflowAtoms, Lists.<BagKey> newArrayList(path));
    }

    private void assertOverflowPath(List<ByteBuffer> overflowAtoms, List<BagKey> keys) {
        assertNotNull(overflowAtoms);
        assertEquals(keys.size() + 1, overflowAtoms.size());
        for (int level = 0; level < keys.size(); level++) {
            BagKey key = keys.get(level);
            ByteBuffer atom = overflowAtoms.get(level);
            assertEquals(atom, HeaderSerialization.serialize(key, level));
            try {
                assertEquals(key, HeaderSerialization.parse(atom));
            } catch (AtomLayerException | BaggageLayerException e) {
                fail(e.getMessage());
                e.printStackTrace();
            }
        }
        assertEquals(BaggageAtoms.OVERFLOW_MARKER, overflowAtoms.get(keys.size()));
    }

    @Before
    public void setUp() throws Exception {
        r.setSeed(0);
    };

    @Test
    public void testWriteReadSimple() {
        ByteBuffer contents = randomBytes(10);

        BaggageWriter writer = BaggageWriter.create();
        ByteBuffers.copyTo(contents, writer.newDataAtom(contents.remaining()));
        BaggageReader reader = BaggageReader.create(writer.atoms());

        assertFalse(reader.didOverflow());
        assertEquals(contents, reader.nextData());
        assertFalse(reader.didOverflow());

        reader.finish();
        assertNull(reader.overflowAtoms());
    }

    @Test
    public void testWriteReadSimpleWithOverflow() {
        ByteBuffer contents = randomBytes(10);

        BaggageWriter writer = BaggageWriter.create();
        writer.didOverflowHere(true);
        ByteBuffers.copyTo(contents, writer.newDataAtom(contents.remaining()));
        BaggageReader reader = BaggageReader.create(writer.atoms());

        assertTrue(reader.didOverflow());
        assertEquals(contents, reader.nextData());
        assertTrue(reader.didOverflow());

        reader.finish();
        assertOverflowPath(reader.overflowAtoms());
    }

    @Test
    public void testWriteReadSimpleWithOverflow2() {
        ByteBuffer contents = randomBytes(10);

        BaggageWriter writer = BaggageWriter.create();
        ByteBuffers.copyTo(contents, writer.newDataAtom(contents.remaining()));
        writer.didOverflowHere(true);
        BaggageReader reader = BaggageReader.create(writer.atoms());

        assertFalse(reader.didOverflow());
        assertEquals(contents, reader.nextData());
        assertTrue(reader.didOverflow());

        reader.finish();
        assertOverflowPath(reader.overflowAtoms());
    }
}
