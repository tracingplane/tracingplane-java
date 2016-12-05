package edu.brown.cs.systems.tracingplane.transit_layer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Baggage2 {

	public static final TransitLayer2<?> transit = TransitLayerFactory.createDefaultTransitLayer();

	/**
	 * Creates a new, empty baggage using the process's default configured
	 * transit layer
	 */
	public static Baggage2 newInstance() {
		return TransitLayer2Compatibility.newInstance(transit);
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
	public static Baggage2 branch(Baggage2 from) {
		return TransitLayer2Compatibility.branch(transit, from);
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
	public static Baggage2 join(Baggage2 left, Baggage2 right) {
		return TransitLayer2Compatibility.join(transit, left, right);
	}

	/**
	 * Uses the process's default configured transit layer.
	 * 
	 * Deserialize a baggage instance from the provided bytes.
	 */
	public static Baggage2 deserialize(byte[] serialized, int offset, int length) {
		return TransitLayer2Compatibility.deserialize(transit, serialized, offset, length);
	}

	public static Baggage2 readFrom(InputStream in) throws IOException {
		return TransitLayer2Compatibility.readFrom(transit, in);
	}

	/**
	 * Uses the process's default configured transit layer.
	 * 
	 * Serialize a baggage instance to its byte representation
	 */
	public static byte[] serialize(Baggage2 baggage) {
		return TransitLayer2Compatibility.serialize(transit, baggage);
	}

	public static void writeTo(OutputStream out, Baggage2 baggage) throws IOException {
		TransitLayer2Compatibility.writeTo(transit, out, baggage);
	}
}
