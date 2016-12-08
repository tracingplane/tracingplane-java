package edu.brown.cs.systems.tracingplane.baggage_layer;

import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.atom_layer.types.TypeUtils;

/**
 * First bit specifies merge behavior. Next three bits specify branch behavior. Next bit specifies serialize behavior.
 * Final three bits unused. *
 */
public class BagOptions implements Comparable<BagOptions> {

    /**
     * Specifies what the baggage layer should do with the data of a bag when merging multiple baggages. Uses the first
     * bit of bag options.
     */
    public static enum MergeBehavior {
        /** Keep all data from both sides of the branch. Default behavior. */
        KeepAll(0),

        /** Keep only the first item from the merged baggages. */
        KeepFirst(1);

        private static final byte mask = TypeUtils.makeByte("10000000");
        private static final int offset = 7;

        public static final MergeBehavior defaultBehavior = KeepAll;

        public final int id;
        public final byte byteValue;

        MergeBehavior(int id) {
            this.id = id;
            this.byteValue = (byte) (id << offset);
        }

        public static MergeBehavior fromByte(byte b) {
            return MergeBehavior.values()[(b & mask) >> offset];
        }

    }

    /**
     * Specifies what the baggage layer should do with the data of a bag when branching. Uses three bits, offset by one
     * bit.
     */
    public static enum BranchBehavior {
        /** Copy the bag to both baggages when branching. This is the default behavior */
        Copy(0),

        /** Drop the bag data when branching */
        Drop(1),

        /** Keep the bag data in the original baggage, don't copy it to the new baggage */
        Keep(2),

        /** Give the bag to the new baggage, don't keep it in the original baggage */
        Give(3),

        /**
         * Divide the bag data between the original and copied baggage. Each data item will exist either in the original
         * baggage, or in the new baggage, but not in both
         */
        Share(4);

        private static final byte mask = TypeUtils.makeByte("01110000");
        private static final int offset = 4;

        public static final BranchBehavior defaultBehavior = Copy;

        public final int id;
        public final byte byteValue;

        BranchBehavior(int id) {
            this.id = id;
            this.byteValue = (byte) (id << offset);
        }

        public static BranchBehavior fromByte(byte b) {
            return BranchBehavior.values()[(b & mask) >> offset];
        }

    }

    /**
     * Specifies what the baggage layer should do with the data of a bag when serializing. Uses one bit, offset by four
     * bits.
     */
    public static enum SerializeBehavior {
        /** Include the bag when serializing baggage */
        Keep(0),

        /** Drop the bag when serializing baggage */
        Drop(1);

        private static final byte mask = TypeUtils.makeByte("00001000");
        private static final int offset = 3;

        public static final SerializeBehavior defaultBehavior = Keep;

        public final int id;
        public final byte byteValue;

        SerializeBehavior(int id) {
            this.id = id;
            this.byteValue = (byte) (id << offset);
        }

        public static SerializeBehavior fromByte(byte b) {
            return SerializeBehavior.values()[(b & mask) >> offset];
        }
    }

    public static final BagOptions defaultOptions;
    private static final BagOptions[] options;
    private static final BagOptions[] values;

    static {
        options = new BagOptions[256];
        values = new BagOptions[MergeBehavior.values().length * BranchBehavior.values().length *
                                SerializeBehavior.values().length];

        int valueIndex = 0;
        for (MergeBehavior merge : MergeBehavior.values()) {
            for (BranchBehavior branch : BranchBehavior.values()) {
                for (SerializeBehavior serialize : SerializeBehavior.values()) {
                    BagOptions bagOptions = new BagOptions(merge, branch, serialize);
                    if (bagOptions.byteValue < 0) {
                        options[256 + bagOptions.byteValue] = bagOptions;
                    } else {
                        options[bagOptions.byteValue] = bagOptions;
                    }
                    values[valueIndex++] = bagOptions;
                }
            }
        }
        defaultOptions = create(MergeBehavior.defaultBehavior, BranchBehavior.defaultBehavior,
                                SerializeBehavior.defaultBehavior);
    }

    public final MergeBehavior merge;
    public final BranchBehavior branch;
    public final SerializeBehavior serialize;
    public final byte byteValue;

    private BagOptions(MergeBehavior merge, BranchBehavior branch, SerializeBehavior serialize) {
        this.merge = merge;
        this.branch = branch;
        this.serialize = serialize;
        this.byteValue = (byte) (merge.byteValue | branch.byteValue | serialize.byteValue);
    }

    public static BagOptions create(MergeBehavior merge, BranchBehavior branch, SerializeBehavior serialize) {
        return valueOf((byte) (merge.byteValue | branch.byteValue | serialize.byteValue));
    }

    /** Returns null if the byte is not valid */
    public static BagOptions valueOf(byte b) {
        if (b < 0) {
            return options[256 + b];
        } else {
            return options[b];
        }
    }

    public static BagOptions[] values() {
        return values;
    }

    public boolean isDefault() {
        return this == defaultOptions;
    }

    @Override
    public String toString() {
        return String.format("[BagOptions Merge=%s Branch=%s Serialize=%s]", merge.name(), branch.name(),
                             serialize.name());
    }

    @Override
    public int compareTo(BagOptions o) {
        return Lexicographic.compare(byteValue, byteValue);
    }

}
