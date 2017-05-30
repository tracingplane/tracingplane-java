package brown.tracingplane.baggageprotocol;

import static org.junit.Assert.assertEquals;
import java.nio.ByteBuffer;
import org.junit.Test;
import brown.tracingplane.atomlayer.AtomLayerException;
import brown.tracingplane.baggageprotocol.AtomPrefixTypes.Level;
import brown.tracingplane.baggageprotocol.AtomPrefixes.IndexedHeaderPrefix;
import brown.tracingplane.baggageprotocol.AtomPrefixes.KeyedHeaderPrefix;

public class TestHeaderSerialization {

    private static int[] indices() {
        int v = 128;
        return new int[] { 0, 1, v - 1, v, (v *= 128) - 1, v, (v *= 128) - 1, v, (v *= 128) - 1, v, Integer.MAX_VALUE,
                           Integer.MIN_VALUE, -1 };
    }

    private static ByteBuffer[] keys() {
        int i = 0;
        return new ByteBuffer[] { ByteBuffer.allocate(i++), ByteBuffer.allocate(i++), ByteBuffer.allocate(i++),
                                  ByteBuffer.allocate(i++), ByteBuffer.allocate(i++), ByteBuffer.allocate(i++),
                                  ByteBuffer.allocate(i++), ByteBuffer.wrap("".getBytes()),
                                  ByteBuffer.wrap("hello".getBytes()), ByteBuffer.wrap("jon".getBytes()), };
    }

    private static int[] levels() {
        int[] levels = new int[Level.LEVELS];
        for (int i = 0; i < levels.length; i++) {
            levels[i] = i;
        }
        return levels;
    }

    private static BagKey.Indexed indexed(int index) {
        return (BagKey.Indexed) BagKey.indexed(index);
    }

    private static BagKey.Indexed indexed(int index, BagOptions options) {
        return (BagKey.Indexed) BagKey.indexed(index, options);
    }

    private static BagKey.Keyed named(ByteBuffer key, BagOptions options) {
        return (BagKey.Keyed) BagKey.keyed(key, options);
    }

    @Test
    public void testSerializedSizeIndexed() {
        int v = 128;
        for (int i = 1; i < 5; i++) {
            assertEquals(i + 1, HeaderSerialization.serializedSize(indexed(v - 1)));
            assertEquals(i + 2, HeaderSerialization.serializedSize(indexed(v)));

            v *= 128;
        }
    }

    @Test
    public void testSerializeIndexed() {
        int index = 7;
        BagOptions options = BagOptions.defaultOptions();
        int level = 4;

        ByteBuffer buf = HeaderSerialization.serialize(indexed(index, options), level);

        assertEquals(IndexedHeaderPrefix.prefixFor(level, options).prefix, buf.get(0));
        assertEquals(index, buf.get(1));
    }

    @Test
    public void testSerializeDeserializeIndexed() throws AtomLayerException, BaggageLayerException {
        ByteBuffer buf = ByteBuffer.allocate(20);
        for (int index : indices()) {
            for (BagOptions options : BagOptions.values()) {
                BagKey.Indexed bagKey = indexed(index, options);
                for (int level : levels()) {
                    buf.position(0);
                    buf.limit(20);
                    HeaderSerialization.writeAtom(buf, bagKey, level);
                    buf.flip();
                    assertEquals(IndexedHeaderPrefix.prefixFor(level, options).prefix, buf.get(0));
                    BagKey parsedKey = HeaderSerialization.parse(buf);
                    assertEquals(bagKey, parsedKey);
                }
            }

        }
    }

    @Test
    public void testSerializeDeserializeKeyed() throws AtomLayerException, BaggageLayerException {
        ByteBuffer buf = ByteBuffer.allocate(20);
        for (ByteBuffer key : keys()) {
            for (BagOptions options : BagOptions.values()) {
                BagKey.Keyed bagKey = named(key, options);
                for (int level : levels()) {
                    String test = String.format("%s %s", level, bagKey);
                    buf.position(0);
                    buf.limit(20);
                    HeaderSerialization.writeAtom(buf, bagKey, level);
                    buf.flip();
                    assertEquals(test, KeyedHeaderPrefix.prefixFor(level, options).prefix, buf.get(0));
                    BagKey parsedKey = HeaderSerialization.parse(buf);
                    assertEquals(test, bagKey, parsedKey);
                }
            }

        }
    }

}
