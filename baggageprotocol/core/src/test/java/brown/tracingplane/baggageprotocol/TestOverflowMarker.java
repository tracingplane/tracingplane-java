package brown.tracingplane.baggageprotocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import org.junit.Test;
import brown.tracingplane.atomlayer.ByteBuffers;

public class TestOverflowMarker extends BaggageTestCase {

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
        BagKey key = indexed(0);

        BaggageWriter writer = BaggageWriter.create();
        writer.enter(key);
        ByteBuffers.copyTo(contents, writer.newDataAtom(contents.remaining()));
        writer.didOverflowHere(true);
        BaggageReader reader = BaggageReader.create(writer.atoms());

        assertFalse(reader.didOverflow());
        assertEquals(key, reader.enter());
        assertFalse(reader.didOverflow());
        assertEquals(contents, reader.nextData());
        assertTrue(reader.didOverflow());

        reader.finish();
        assertOverflowPath(reader.overflowAtoms(), key);
    }

    @Test
    public void testFirstOverflowPathTaken() {
        BaggageWriter writer = BaggageWriter.create();
        writer.enter(indexed(3));
        writer.enter(indexed(5)).exit();
        writer.enter(indexed(6)).didOverflowHere(true).exit();
        writer.exit();
        writer.enter(indexed(10)).didOverflowHere(true).exit();

        BaggageReader reader = BaggageReader.create(writer.atoms());
        reader.finish();
        assertTrue(reader.didOverflow());

        assertOverflowPath(reader.overflowAtoms(), indexed(3), indexed(6));
    }
}
