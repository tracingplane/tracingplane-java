package brown.tracingplane.impl;

import java.util.function.BiFunction;
import java.util.function.Function;
import brown.tracingplane.BaggageContext;
import brown.tracingplane.BaggageProvider;

/**
 * A {@link BaggageListener} is a callback handler that is invoked whenever core {@link BaggageProvider} functions
 * (branch, merge, etc.) are invoked. Baggage Listeners are able to manipulate baggage instances before and after the
 * functions are called.
 * 
 * A {@link BaggageProvider} must support adding {@link BaggageListener}s.
 */
public final class BaggageListener {

    /** BaggageListeners is not instantiable */
    private BaggageListener() {}

    /**
     * Wraps calls to {@link BaggageProvider#branch(BaggageContext)} so that the baggage instance(s) can be modified.
     */
    @FunctionalInterface
    public static interface BranchListener<B extends BaggageContext> {

        /**
         * Invoked when {@link BaggageProvider#branch(BaggageContext)} is called. <code>wrapped</code> is the default
         * implementation of branch which should be called. This method can optionally perform additional logic before
         * or after invocation, or override its behavior completely.
         * 
         * @param from a baggage context, possibly null
         * @param wrapped the default {@link BaggageProvider#branch(BaggageContext)} function
         * @return a baggage context branched from <code>from</code>, possibly null
         */
        public B branch(B from, Function<B, B> wrapped);
    }

    /**
     * Wraps calls to {@link BaggageProvider#join(BaggageContext, BaggageContext)} so that baggage instance(s) can be
     * modified.
     */
    @FunctionalInterface
    public static interface JoinListener<B extends BaggageContext> {

        /**
         * Invoked when {@link BaggageProvider#join(BaggageContext, BaggageContext)} is called. <code>wrapped</code> is
         * the default implementation of join which should be called. This method can optionally perform additional
         * logic before or after invocation, or override its behavior completely.
         * 
         * @param left a {@link BaggageContext} instance, possibly null
         * @param right a {@link BaggageContext} instance, possibly null
         * @param next the default {@link BaggageProvider#join(BaggageContext, BaggageContext)} function
         * @return a baggage context with merged contents from <code>left</code> and <code>right</code>
         */
        public B join(B left, B right, BiFunction<B, B, B> wrapped);
    }
    
}
