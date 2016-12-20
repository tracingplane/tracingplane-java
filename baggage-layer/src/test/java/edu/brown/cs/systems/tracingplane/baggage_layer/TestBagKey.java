package edu.brown.cs.systems.tracingplane.baggage_layer;

import static org.junit.Assert.assertEquals;
import java.nio.ByteBuffer;
import org.junit.Test;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;

public class TestBagKey {

    @Test
    public void testBagKeyComparison() {
        int[] indicesToTest =
                { Integer.MIN_VALUE, Short.MIN_VALUE, -10, -3, -1, 0, 1, 3, 10, Short.MAX_VALUE, Integer.MAX_VALUE };
        String[] keysToTest = { "", "a", "aaaaa", "hello", "hell", "boo" };
        ByteBuffer[] bufs = new ByteBuffer[keysToTest.length];
        for (int i = 0; i < keysToTest.length; i++) {
            bufs[i] = ByteBuffer.wrap(keysToTest[i].getBytes());
        }

        for (int i : indicesToTest) {
            for (int j : indicesToTest) {
                assertEquals(Integer.compare(i, j), BagKey.indexed(i).compareTo(BagKey.indexed(j)));
            }
            for (String key : keysToTest) {
                assertEquals(-1, BagKey.indexed(i).compareTo(BagKey.named(key)));
                assertEquals(1, BagKey.named(key).compareTo(BagKey.indexed(i)));
            }
            for (ByteBuffer buf : bufs) {
                assertEquals(-1, BagKey.indexed(i).compareTo(BagKey.keyed(buf)));
                assertEquals(1, BagKey.keyed(buf).compareTo(BagKey.indexed(i)));
            }
        }

        for (ByteBuffer a : bufs) {
            for (ByteBuffer b : bufs) {
                assertEquals(Lexicographic.compare(a, b), BagKey.keyed(a).compareTo(BagKey.keyed(b)));
            }
        }
    }

}
