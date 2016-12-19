package edu.brown.cs.systems.tracingplane.baggage_layer.todo;

import java.nio.ByteBuffer;
import java.util.List;
import com.google.common.collect.Lists;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;

public abstract class TestSerializationDeserializationSpeed {
    
    int numtrials = 10;
    int batchsize = 1000;
    int triallengthMs = 1000;
    
    public long doTrial() {
        long now = System.currentTimeMillis();
        long endAt = now + triallengthMs;
        long total = 0;
        while ((now = System.currentTimeMillis()) < endAt) {
            for (int i = 0; i < batchsize; i++) {
                doTest();
                total++;
            }
        }
        return total;
    }
    
    public void runTrials() {
        for (int i = 0; i < numtrials; i++) {
            long begin = System.currentTimeMillis();
            long total = doTrial();
            long end = System.currentTimeMillis();
            
            long speed = (1000 * total) / (end - begin);
            System.out.printf("Trial %d: %d total, %d/s\n", i, total, speed);
        }
    }
    
    public abstract Object doTest();
    
    private abstract static class TestDeserialize extends TestSerializationDeserializationSpeed {
        
        private int numInstances = 1000;
        private final List<ByteBuffer>[] atomses;
        
        public TestDeserialize() {
            atomses = new List[numInstances];
            for (int i = 0; i < numInstances; i++) {
                atomses[i] = makeAtoms();
            }
        }
        
        public abstract List<ByteBuffer> makeAtoms();
        
        public List<ByteBuffer> getAtoms(int i) {
            return atomses[i % numInstances];
        }
        
    }
    
    private static class TestSimpleDeserialize extends TestDeserialize {

        private static final BagKey outer = BagKey.indexed(0);
        private static final BagKey inner = BagKey.indexed(0);
        private static final BagKey inner2 = BagKey.indexed(0);

        private static final BagKey outer2 = BagKey.indexed(0);
        
        

        @Override
        public List<ByteBuffer> makeAtoms() {
            BaggageWriter writer = BaggageWriter.create();
            writer.enter(outer);
            writer.enter(inner);
            writer.newDataAtom(8).putLong(77);
            writer.exit();
            writer.enter(inner2);
            writer.newDataAtom(8).putLong(7);
            writer.newDataAtom(8).putLong(2);
            writer.newDataAtom(8).putLong(1000);
            writer.exit();
            writer.exit();
            writer.enter(outer2);
            writer.newDataAtom(8).putLong(55);
            writer.exit();
            return writer.atoms();
        }
        
        private int i = 0;
        
        static class MyObject {
            Long a;
            List<Long> bs;
            Integer c;
        }

        @Override
        public Object doTest() {
            BaggageReader reader = BaggageReader.create(getAtoms(i++));
            MyObject mine = new MyObject();
            if (reader.enter(outer)) {
                if (reader.enter(inner)) {
                    ByteBuffer next = reader.nextData();
                    if (next != null && next.remaining() == 8) {
                        mine.a = next.getLong();
                    }
                    reader.exit();
                }
                if (reader.enter(inner2)) {
                    mine.bs = Lists.newArrayList();
                    while (reader.hasData()) {
                        ByteBuffer next = reader.nextData();
                        if (next != null && next.remaining() == 8) {
                            mine.bs.add(next.getLong());
                        }
                    }
                    reader.exit();
                }
                reader.exit();
            }
            if (reader.enter(outer2)) {
                ByteBuffer next = reader.nextData();
                if (next != null && next.remaining() == 4) {
                    mine.c = next.getInt();
                }
            }
            return mine;
        }
        
    }
    
    public static void main(String[] args) {
        
        new TestSimpleDeserialize().runTrials();
    }

}
