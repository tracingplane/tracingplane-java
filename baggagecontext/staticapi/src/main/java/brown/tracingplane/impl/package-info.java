/**
 * <p>
 * Implementations of {@link BaggageContext} at several different points in the Tracing Plane.
 * </p>
 * 
 * <p>
 * {@link AtomContext} is the most lightweight atom-based context, which provides no accessor or convenience methods for
 * manipulating data. {@link AtomContext} is purely for propagation (e.g., forwarding baggage contexts that you received
 * from other network calls).
 * </p>
 * 
 * <p>
 * {@link NestedBaggageContext} extends the Atom Context and implements the Baggage Protocol, which gives nested data
 * structures.
 * </p>
 * 
 * <p>
 * {@link BDLContext} is the full-featured baggage context, which provides interpretations for different data types
 * using atoms.
 * </p>
 * 
 */
package brown.tracingplane.impl;
