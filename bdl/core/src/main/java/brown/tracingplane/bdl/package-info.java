/**
 * <p>
 * Library classes used by BDL-generated objects, including the {@link BDLContext} implementation of
 * {@link BaggageContext}.
 * </p>
 * 
 * <p>
 * The two main interfaces used by BDL are {@link Bag} and {@link BaggageHandler}. {@link Bag}s are nodes in the nested
 * data structure tree, while {@link BaggageHandler}s provide branch, join, and serialization logic for {@link Bag}s.
 * {@link Bag} and {@link BaggageHandler} are conceptually similar to {@link BaggageContext} and {@link BaggageProvider}
 * , except applying to nodes within a {@link BDLContext}.
 * </p>
 * 
 * <p>
 * The BDL library classes also include CRDT-like data structures that the BDL supports, such as counters, implemented
 * by {@link CounterImpl}.
 * </p> 
 */
package brown.tracingplane.bdl;
