package edu.brown.cs.systems.tracingplane.transit_layer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//TODO: description and method documentation
public interface Baggage {

	public static final TransitLayer<?> transit = TransitLayerFactory.createDefaultTransitLayer();

	/**
	 * Creates a new, empty baggage using the process's default configured
	 * transit layer
	 */
	public static Baggage newInstance() {
		return TransitLayerCompatibility.newInstance(transit);
	}

	/**
	 * Discards the provided baggage, which should not be used again after
	 * calling this method.
	 */
	public static void discard(Baggage baggage) {
		TransitLayerCompatibility.discard(transit, baggage);
	}

	/**
	 * Uses the process's default configured transit layer.
	 * 
	 * Creates a new baggage instance based off the provided instance. The
	 * provided instance is still valid and might be modified by this operation.
	 * Might return the original baggage instance.
	 * 
	 * This operation is typically used when an execution is branching into
	 * multiple concurrent components; for example, a second thread is being
	 * started, or callbacks are being enqueued to a thread pool. Each branch of
	 * the execution should have its own baggage instance.
	 * 
	 * @param from
	 *            a baggage instance
	 * @return a baggage instance
	 */
	public static Baggage branch(Baggage from) {
		return TransitLayerCompatibility.branch(transit, from);
	}

	/**
	 * Uses the process's default configured transit layer.
	 * 
	 * Creates a new baggage instance based off two other instances.
	 * 
	 * This operation is typically used when two concurrent branches of an
	 * execution are joining; for example, if one thread calls join on another
	 * thread; or if concurrent parents of a task complete, enabling the child
	 * task to begin.
	 * 
	 * @param left
	 *            a baggage instance
	 * @param right
	 *            a baggage instance
	 * @return a baggage instance
	 */
	public static Baggage join(Baggage left, Baggage right) {
		return TransitLayerCompatibility.join(transit, left, right);
	}

	/**
	 * Uses the process's default configured transit layer.
	 * 
	 * Deserialize a baggage instance from the provided bytes.
	 */
	public static Baggage deserialize(byte[] serialized, int offset, int length) {
		return TransitLayerCompatibility.deserialize(transit, serialized, offset, length);
	}

	public static Baggage readFrom(InputStream in) throws IOException {
		return TransitLayerCompatibility.readFrom(transit, in);
	}

	/**
	 * Uses the process's default configured transit layer.
	 * 
	 * Serialize a baggage instance to its byte representation
	 */
	public static byte[] serialize(Baggage baggage) {
		return TransitLayerCompatibility.serialize(transit, baggage);
	}

	public static void writeTo(OutputStream out, Baggage baggage) throws IOException {
		TransitLayerCompatibility.writeTo(transit, out, baggage);
	}

	/**
	 * Gets the Baggage instance, if any, being stored for the current thread
	 * 
	 * @return a Baggage instance, possibly null
	 */
	public static Baggage get() {
		return ThreadLocalBaggage.get();
	}
	
	/**
	 * Removes the baggage instance being stored in the current thread
	 */
	public static void discard() {
		ThreadLocalBaggage.discard();
	}

	/**
	 * Removes and returns the baggage instance being stored in the current
	 * thread
	 */
	public static Baggage take() {
		return ThreadLocalBaggage.take();
	}

	/**
	 * Set the Baggage instance for the current thread
	 * 
	 * @param baggage
	 *            a Baggage instance, possibly null
	 */
	public static void set(Baggage baggage) {
		ThreadLocalBaggage.set(baggage);
	}
}
