package edu.brown.cs.systems.tracingplane.baggage_buffers;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import edu.brown.cs.systems.tracingplane.atom_layer.types.TypeUtils;
import edu.brown.cs.systems.tracingplane.baggage_buffers.api.Bag;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;

public class BaggageBuffersUtils {
    
    private BaggageBuffersUtils(){}

    /**
     * Ideally, this is an instance method, but for now we're doing it here
     */
    public static int serializedSize(Baggage instance) {
        if (instance == null) {
            return 0;
        }
        byte[] serialized = Baggage.serialize(instance);
        return serialized == null ? 0 : serialized.length;
    }

    /**
     * Ideally, this is an instance method, but for now we're doing it here
     */
    public static int serializedSizeOfBag(Bag bag) {
        if (bag != null) {
            BaggageWriter writer = BaggageWriter.create();
            bag.handler().serialize(writer, bag);
            int size = 0;
            for (ByteBuffer atom : writer.atoms()) {
                size += 1 + atom.remaining();
            }
            return size;
        }
        return 0;
    }

    /**
     * Ideally, this is an instance method, but for now we're doing it here
     */
    public static int serializedSize (BagKey key, Bag bag) {
        if (bag != null) {
            BaggageWriter writer = BaggageWriter.create();
            writer.enter(key);
            bag.handler().serialize(writer, bag);
            writer.exit();
            int size = 0;
            for (ByteBuffer atom : writer.atoms()) {
                size += 1 + atom.remaining();
            }
            return size;
        }
        return 0;
    }
    
    public static Map<String, String> getSizeSummary(Baggage instance) {
        if (instance == null || !(instance instanceof BaggageBuffersContents)) {
            return Collections.emptyMap();
        }
        return getSizeSummary((BaggageBuffersContents) instance);
    }
    
    public static Map<String, String> getSizeSummary(BaggageBuffersContents instance) {
        if (instance == null) {
            return Collections.emptyMap();            
        }
        
        Map<String, String> summary = new HashMap<>();
        summary.put("BaggageTotalSize", String.valueOf(serializedSize(instance)));
        
        if (instance.bags != null) {
            for (BagKey key : instance.bags.keySet()) {
                Bag bag = instance.bags.get(key);
                if (bag != null) {
                    String name = bag.getClass().getSimpleName();
                    summary.put(name, String.valueOf(serializedSize(key, bag)));
                }
            }
        }
        if (instance.overflowAtoms != null) {
            summary.put("OverflowAtoms", TypeUtils.toHexString(instance.overflowAtoms, ","));
        }
        if (instance.unprocessedAtoms != null) {
            summary.put("UnprocessedAtoms", TypeUtils.toHexString(instance.overflowAtoms, ","));            
        }
        
        return summary;
    }
    
    /** Utility for seeing who accesses baggage and where */
    public static interface BaggageAccessListener {
        public boolean enter();
        public void get(BagKey bagKey);
        public void set(BagKey bagKey);
        public void compact();
        public void exit();
    }
    
    public static class NullBaggageListener implements BaggageAccessListener {
        public boolean enter() { return false; }
        public void get(BagKey bagKey) {}
        public void set(BagKey bagKey) {}
        public void compact() {}
        public void exit() {}
    }
    
    // This is a hack to add 'compaction' joins without deploying them to all the interfaces.
    // At this point in time it's still a question of whether compact joins are useful
    public static final ThreadLocal<Boolean> is_compaction = new ThreadLocal<Boolean>() {
        @Override public Boolean initialValue() {
            return false;
        }
    };

}
