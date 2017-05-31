package brown.tracingplane.impl;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import brown.tracingplane.BaggageContext;
import brown.tracingplane.atomlayer.AtomLayerSerialization;
import brown.tracingplane.atomlayer.TypeUtils;
import brown.tracingplane.baggageprotocol.BagKey;
import brown.tracingplane.baggageprotocol.BaggageWriter;
import brown.tracingplane.bdl.Bag;

/**
 * <p>Helper methods for {@link BDLContext} instances -- calculating serialized size, printing context contents, etc.</p>
 */
public class BDLContextUtils {

    private BDLContextUtils() {}

    /**
     * Ideally, this is an instance method, but for now we're doing it here
     */
    public static int serializedSize(BaggageContext instance) {
        if (instance == null || !(instance instanceof BDLContext)) {
            return 0;
        } else {
            return AtomLayerSerialization.serializedSize(((BDLContext) instance).serialize().atoms());
        }
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
    public static int serializedSize(BagKey key, Bag bag) {
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

    public static Map<String, String> getSizeSummary(BaggageContext instance) {
        if (instance == null || !(instance instanceof BDLContext)) {
            return Collections.emptyMap();
        }
        return getSizeSummary((BDLContext) instance);
    }

    public static Map<String, String> getSizeSummary(BDLContext instance) {
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
        public boolean enter() {
            return false;
        }

        public void get(BagKey bagKey) {}

        public void set(BagKey bagKey) {}

        public void compact() {}

        public void exit() {}
    }

}
