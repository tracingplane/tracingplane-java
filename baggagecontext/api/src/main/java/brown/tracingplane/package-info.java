/**
 * <p>
 * Provides the main {@link BaggageContext} and {@link BaggageProvider} interfaces of the Tracing Plane
 * </p>
 * 
 * <p>
 * At a high level, {@link BaggageContext} instances exist at the granularity of requests (or tasks, jobs, etc). The
 * purpose is to propagate a {@link BaggageContext} alongside each request while it executes. {@link BaggageContext}s
 * carry user-defined or tracing-tool defined data.
 * </p>
 * 
 * <p>
 * {@link BaggageContext} instances should follow requests in a fine-grained manner. For example, if a request splits
 * off into multiple concurrent execution branches, then each branch of execution should receive its its own
 * {@link BaggageContext} instance, created by calling {@link BaggageProvider#branch(BaggageContext)} at the time the
 * request splits.
 * </p>
 * 
 * <p>
 * Likewise, if multiple concurrent branches merge, or if some task is dependent upon multiple predecessors completing,
 * then {@link BaggageContext} instances can be merged using
 * {@link BaggageProvider#join(BaggageContext, BaggageContext)}.
 * </p>
 * 
 * <p>
 * The Tracing Plane provides several {@link BaggageContext} implementations, the main implementation being
 * {@link brown.tracingplane.impl.BDLContext} in the {@link brown.tracingplane.bdl} package.
 * </p>
 */
package brown.tracingplane;
