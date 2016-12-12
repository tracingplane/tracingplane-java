package edu.brown.cs.systems.tracingplane.atom_layer.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.Test;
import com.google.common.collect.Lists;
import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.atom_layer.protocol.AtomLayerSerialization;

public class TestOverflowMarker {

    @Test
    public void testSerializedOverflowMarker() {
        int overflowMarkerSerializedSize = AtomLayerSerialization.serializedSize(BaggageAtoms.OVERFLOW_MARKER);
        assertEquals(1, overflowMarkerSerializedSize);

        ByteBuffer buf = ByteBuffer.allocate(overflowMarkerSerializedSize);
        AtomLayerSerialization.writeAtom(BaggageAtoms.OVERFLOW_MARKER, buf);

        assertEquals(0, buf.remaining());

        buf.rewind();
        byte b = buf.get();
        assertEquals(0, b);

        buf.rewind();
        List<ByteBuffer> deserialized =
                AtomLayerSerialization.deserialize(buf.array(), buf.position(), buf.remaining());
        assertNotNull(deserialized);
        assertEquals(1, deserialized.size());
        assertEquals(BaggageAtoms.OVERFLOW_MARKER, deserialized.get(0));
    }

    @Test
    public void testTrimByteBuffer() {
        ByteBuffer b1 = ByteBuffer.allocate(4);
        b1.putInt(77);
        b1.rewind();

        ByteBuffer b2 = ByteBuffer.allocate(4);
        b1.putInt(88);
        b1.rewind();

        List<ByteBuffer> bufs = Lists.newArrayList(b1, b2);

        assertEquals(10, AtomLayerSerialization.serializedSize(bufs));

        for (int i = 1; i < 6; i++) {
            List<ByteBuffer> trimmed = AtomLayerOverflow.trimToSize(bufs, i);
            assertNotNull(trimmed);
            assertEquals(1, trimmed.size());
            assertFalse(b1.equals(trimmed.get(0)));
            assertEquals(BaggageAtoms.OVERFLOW_MARKER, trimmed.get(0));
        }

        for (int i = 6; i < 10; i++) {
            List<ByteBuffer> trimmed = AtomLayerOverflow.trimToSize(bufs, i);
            assertNotNull(trimmed);
            assertEquals(2, trimmed.size());
            assertEquals(b1, trimmed.get(0));
            assertFalse(b2.equals(trimmed.get(1)));
            assertEquals(BaggageAtoms.OVERFLOW_MARKER, trimmed.get(1));
        }

        for (int i = 10; i < 20; i++) {
            List<ByteBuffer> trimmed = AtomLayerOverflow.trimToSize(bufs, i);
            assertNotNull(trimmed);
            assertEquals(2, trimmed.size());
            assertEquals(b1, trimmed.get(0));
            assertEquals(b2, trimmed.get(1));
            assertEquals(bufs, trimmed);
        }

    }

}
