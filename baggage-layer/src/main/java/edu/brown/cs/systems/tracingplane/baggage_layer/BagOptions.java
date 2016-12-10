package edu.brown.cs.systems.tracingplane.baggage_layer;

/**
 * <p>
 * The only additional option that we propagate is the merge behavior. We do not propagate serialize or branch
 * behaviors, as follows:
 * </p>
 * 
 * <ul>
 * <li>The main use case for specifying branch and serialize behaviors is to handle 'brittle' fields. The default
 * propagation behavior is `ignore and propagate`; however, you might want to override this to drop data in some
 * scenarios. For example, you might have a vector clock style component id in the baggage and could want to ensure that
 * only one side of the branch retains the ID.</li>
 * <li>Because of this it could be argued that the baggage layer could have some options for specifying logic for when
 * baggage operations (branch, join, serialize) occur.</li>
 * <li>The reason for implementing logic in the baggage layer is for when data traverses other processes that are
 * baggage compliant, but lack knowledge of the semantics of specific fields of the baggage</li>
 * <li>However, when the baggage leaves the current process, it might traverse atom-compliant-only processes (ie,
 * non-baggage-compliant) that naively copy the data (because they adhere to `ignore and propagate`. This is
 * unavoidable. So we cannot provide guarantees for brittle fields outside of a process.</li>
 * <li>Within a process, if the process knows how to add one of these brittle fields to the baggage, then it is implied
 * that the process knows the semantics of that field. Thus, the process also has control of when the baggage will
 * branch, join, and serialize within this process. This means it can interpose on the baggage via callbacks</li>
 * <li>As a result, we argue that branch and serialize options are unnecessary and can be handled by callbacks</li>
 * </ul>
 */
public class BagOptions implements Comparable<BagOptions> {

    private static final BagOptions[] values =
            { new BagOptions(MergeBehavior.TakeAll), new BagOptions(MergeBehavior.TakeFirst) };
    public static final BagOptions defaultOptions = values[0];

    public static enum MergeBehavior {
        TakeAll, TakeFirst;
    }

    public final MergeBehavior merge;

    private BagOptions(MergeBehavior merge) {
        this.merge = merge;
    }

    public static BagOptions defaultOptions() {
        return defaultOptions;
    }

    public static BagOptions create(MergeBehavior mergeBehavior) {
        return values[mergeBehavior.ordinal()];
    }

    public boolean isDefault() {
        return this == defaultOptions;
    }

    @Override
    public String toString() {
        return String.format("[BagOptions Merge=%s]", merge.name());
    }

    @Override
    public int compareTo(BagOptions o) {
        return merge.compareTo(o.merge);
    }
    
    public static BagOptions[] values() {
        return values;
    }

}
