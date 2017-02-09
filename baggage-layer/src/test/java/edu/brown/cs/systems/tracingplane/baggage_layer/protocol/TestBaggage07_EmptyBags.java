package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.Test;
import com.google.common.collect.Lists;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;

/**
 * Tests a baggage containing a bag that contains a bag that contains ...
 * 
 * Checks that if an empty bag is added, then it isn't included in serialized form
 */
public class TestBaggage07_EmptyBags extends BaggageTestCase {

    private final BagKey.Indexed A = indexed(4);
    private final BagKey.Indexed AA = indexed(9);
    private final BagKey.Indexed AAA = indexed(8282);

    private final List<ByteBuffer> payloadA = Lists.newArrayList(randomBytes(100));

    private void writePayloads(BaggageWriter writer, List<ByteBuffer> payloads) {
        payloads.forEach(p -> ByteBuffers.copyTo(p, writer.newDataAtom(p.remaining())));
    }

    private BaggageReader makeBaggage() {
        BaggageWriter writer = BaggageWriter.create();

        {
            writer.enter(A);
            writePayloads(writer, payloadA);
            writer.enter(AA).enter(AAA);
            writer.exit().exit();
            writer.exit();
        }

        return BaggageReader.create(writer.atoms());
    }

    @Test
    public void testEnter() {
        BaggageReader reader = makeBaggage();
        assertTrue(reader.enter(A));
        assertFalse(reader.enter(AA));
        assertFalse(reader.hasNext());
        reader.exit();
    }
}
