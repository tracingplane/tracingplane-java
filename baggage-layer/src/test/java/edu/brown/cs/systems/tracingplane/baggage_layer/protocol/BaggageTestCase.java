package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;

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

}
