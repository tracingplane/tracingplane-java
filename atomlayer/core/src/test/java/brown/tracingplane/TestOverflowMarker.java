package brown.tracingplane;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import com.google.common.collect.Lists;
import brown.tracingplane.atomlayer.AtomLayerOverflow;
import brown.tracingplane.atomlayer.AtomLayerSerialization;

public class TestOverflowMarker {

    @Test
    public void testSerializedOverflowMarker() {
        int overflowMarkerSerializedSize = AtomLayerSerialization.serializedSize(AtomLayerOverflow.OVERFLOW_MARKER);
        assertEquals(1, overflowMarkerSerializedSize);

        ByteBuffer buf = ByteBuffer.allocate(overflowMarkerSerializedSize);
        AtomLayerSerialization.writeAtom(AtomLayerOverflow.OVERFLOW_MARKER, buf);

        assertEquals(0, buf.remaining());

        buf.rewind();
        byte b = buf.get();
        assertEquals(0, b);

        buf.rewind();
        List<ByteBuffer> deserialized =
                AtomLayerSerialization.deserialize(buf.array(), buf.position(), buf.remaining());
        assertNotNull(deserialized);
        assertEquals(1, deserialized.size());
        assertEquals(AtomLayerOverflow.OVERFLOW_MARKER, deserialized.get(0));
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
            assertEquals(AtomLayerOverflow.OVERFLOW_MARKER, trimmed.get(0));
        }

        for (int i = 6; i < 10; i++) {
            List<ByteBuffer> trimmed = AtomLayerOverflow.trimToSize(bufs, i);
            assertNotNull(trimmed);
            assertEquals(2, trimmed.size());
            assertEquals(b1, trimmed.get(0));
            assertFalse(b2.equals(trimmed.get(1)));
            assertEquals(AtomLayerOverflow.OVERFLOW_MARKER, trimmed.get(1));
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
    
    @Test
    public void testMergeOverflowAtoms() {
        List<ByteBuffer> nullList = null;
        List<ByteBuffer> emptyList = new ArrayList<ByteBuffer>();
        List<ByteBuffer> justMarker = Lists.<ByteBuffer>newArrayList(AtomLayerOverflow.OVERFLOW_MARKER);
        
        assertEquals(nullList, AtomLayerOverflow.mergeOverflowAtoms(nullList, nullList));
        assertEquals(emptyList, AtomLayerOverflow.mergeOverflowAtoms(emptyList, nullList));
        assertEquals(emptyList, AtomLayerOverflow.mergeOverflowAtoms(nullList, emptyList));
        assertEquals(emptyList, AtomLayerOverflow.mergeOverflowAtoms(emptyList, emptyList));

        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(justMarker, nullList));
        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(justMarker, emptyList));
        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(justMarker, justMarker));
        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(nullList, justMarker));
        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(emptyList, justMarker));
        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(justMarker, justMarker));

        ByteBuffer a = ByteBuffer.allocate(77);
        List<ByteBuffer> aFirst = Lists.newArrayList(a, AtomLayerOverflow.OVERFLOW_MARKER);

        assertEquals(aFirst, AtomLayerOverflow.mergeOverflowAtoms(aFirst, nullList));
        assertEquals(aFirst, AtomLayerOverflow.mergeOverflowAtoms(aFirst, emptyList));
        assertEquals(aFirst, AtomLayerOverflow.mergeOverflowAtoms(aFirst, aFirst));
        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(aFirst, justMarker));

        assertEquals(aFirst, AtomLayerOverflow.mergeOverflowAtoms(nullList, aFirst));
        assertEquals(aFirst, AtomLayerOverflow.mergeOverflowAtoms(emptyList, aFirst));
        assertEquals(aFirst, AtomLayerOverflow.mergeOverflowAtoms(aFirst, aFirst));
        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(justMarker, aFirst));

        List<ByteBuffer> aLast = Lists.newArrayList(AtomLayerOverflow.OVERFLOW_MARKER, a);

        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(aLast, nullList));
        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(aLast, emptyList));
        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(aLast, aLast));
        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(aLast, aFirst));
        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(aLast, justMarker));

        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(nullList, aLast));
        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(emptyList, aLast));
        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(aLast, aLast));
        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(aFirst, aLast));
        assertEquals(justMarker, AtomLayerOverflow.mergeOverflowAtoms(justMarker, aLast));


        ByteBuffer b = ByteBuffer.allocate(4);
        ByteBuffer c = ByteBuffer.allocate(8);
        ByteBuffer d = ByteBuffer.allocate(12);
        
        List<ByteBuffer> test1 = Lists.newArrayList(b, c, a, AtomLayerOverflow.OVERFLOW_MARKER, d);
        List<ByteBuffer> test2 = Lists.newArrayList(b, c, a, AtomLayerOverflow.OVERFLOW_MARKER);

        assertEquals(test2, AtomLayerOverflow.mergeOverflowAtoms(test1, test2));
        assertEquals(test2, AtomLayerOverflow.mergeOverflowAtoms(test1, test1));
        assertEquals(test2, AtomLayerOverflow.mergeOverflowAtoms(test2, test2));
        assertEquals(test2, AtomLayerOverflow.mergeOverflowAtoms(test1, aFirst));
        assertEquals(test2, AtomLayerOverflow.mergeOverflowAtoms(test2, aFirst));
        
    }

}
