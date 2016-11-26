package edu.brown.cs.systems.tracingplane.transit_layer;

import java.io.InputStream;
import java.io.OutputStream;

public interface Baggage {

	public static final TransitLayer transit = TransitLayerConfig.newTransitLayer();

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
		return transit.branch(from);
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
		return transit.join(left, right);
	}

	/**
	 * Uses the process's default configured transit layer.
	 * 
	 * Deserialize a baggage instance from the provided bytes.
	 */
	public static Baggage deserialize(byte[] serialized, int offset, int length) {
		return transit.deserialize(serialized, offset, length);
	}

	public static Baggage readFrom(InputStream in) {
		return transit.readFrom(in);
	}

	/**
	 * Uses the process's default configured transit layer.
	 * 
	 * Serialize a baggage instance to its byte representation
	 */
	public static byte[] serialize(Baggage instance) {
		return transit.serialize(instance);
	}

	public static void writeTo(Baggage instance, OutputStream out) {
		transit.writeTo(instance, out);
	}
}
