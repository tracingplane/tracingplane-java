package brown.tracingplane;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * <p>
 * 
 * A {@link BaggageListeners} receives callbacks when basic {@link BaggageContext} operations are invoked. It is up to
 * the {@link BaggageProvider} to ensure that callbacks are called.
 * </p>
 */
public final class BaggageListeners {

    /** BaggageListeners is not instantiable */
    private BaggageListeners() {}

    /**
     * Intercepts calls to {@link BaggageProvider#newInstance()}
     */
    @FunctionalInterface
    public static interface NewInstanceListener<B extends BaggageContext> {
        /**
         * Called after a new {@link BaggageContext} instance is created. This method can modify the provided instance
         * or create a new one.
         * 
         * @param instance the just-now created {@link BaggageContext} instance
         * @return a baggage instance; by default should just return {@code instance}
         */
        public B postNewInstance(B instance);
    }

    /**
     * Intercepts calls to {@link BaggageProvider#discard(BaggageContext)} prior to discarding each
     * {@link BaggageContext}.
     */
    @FunctionalInterface
    public static interface DiscardListener<B extends BaggageContext> {

        /**
         * Invoked when {@link BaggageProvider#discard(BaggageContext)} is called, prior to discarding the actual
         * baggage.
         * 
         * @param toBeDiscarded a baggage instance
         * @return a baggage instance; by default returns {@code toBeDiscarded}
         */
        public B preDiscard(B toBeDiscarded);
    }

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
