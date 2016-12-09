package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import com.google.common.collect.Lists;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.atom_layer.types.AtomLayerException;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageLayerException;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.DataPrefix;

public abstract class BaggageTestCase {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    final Random r = new Random(0);

    ByteBuffer randomBytes(int length) {
        byte[] bytes = new byte[length];
        r.nextBytes(bytes);
        return ByteBuffer.wrap(bytes);
    }

    BagKey.Indexed indexed(int index) {
        return (BagKey.Indexed) BagKey.indexed(index);
    }

    ByteBuffer dataAtom(ByteBuffer payload) {
        ByteBuffer atom = ByteBuffer.allocate(payload.remaining() + 1);
        atom.put(DataPrefix.prefix);
        ByteBuffers.copyTo(payload, atom);
        atom.flip();
        return atom;
    }

    ByteBuffer headerAtom(BagKey bagKey, int level) {
        return HeaderSerialization.serialize(bagKey, level);
    }

    List<ByteBuffer> writeBag(BagKey key, ByteBuffer... contents) {
        BaggageWriter writer = BaggageWriter.create();
        writer.enter(key);
        for (ByteBuffer content : contents) {
            ByteBuffers.copyTo(content, writer.newDataAtom(content.remaining()));
        }
        writer.exit();
        return writer.atoms();
    }

    List<ByteBuffer> writeBag(BagKey a, BagKey b, ByteBuffer... contents) {
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

    @Before
    public void setRandomSeed() throws Exception {
        r.setSeed(0);
    };

    void assertOverflowPath(List<ByteBuffer> overflowAtoms, BagKey... path) {
        assertOverflowPath(overflowAtoms, Lists.<BagKey> newArrayList(path));
    }

    void assertOverflowPath(List<ByteBuffer> overflowAtoms, List<BagKey> keys) {
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
}
