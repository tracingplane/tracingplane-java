package edu.brown.cs.systems.tracingplane.baggage_layer.impl_old3;

import java.nio.ByteBuffer;
import java.util.Iterator;

public interface BagParser<T> {

	/**
	 * Parse the data that was found in a bag, and turn it into an object.
	 * This method can return null. This method does not need to exhaust the
	 * iterator.
	 */
	public T parse(Iterator<ByteBuffer> data);

	/**
	 * Called to indicate that we hit an overflow marker while parsing this
	 * bag's direct data (eg, before reaching its children)
	 * 
	 * The default implementation of this method does nothing. Override this
	 * method if you wish to track overflow.
	 */
	public default void markDataOverflow(T object) {
	}
	
	public default BagParser<?> getParserForChild(int childIndex, ByteBuffer childOptions) {
		return null;
	}
	
	public default BagParser<?> getParserForChild(ByteBuffer childKey, ByteBuffer childOptions) {
		return null;
	}
	
	public default void setChild(int childIndex, ByteBuffer childOptions, T parent, Object child) {
	}
	
	public default void setChild(ByteBuffer childKey, ByteBuffer childOptions, T parent, Object child) {
	}

	/**
	 * Called to indicate that we hit an overflow marker after parsing this
	 * bag's direct data (eg, while parsing its children)
	 * 
	 * The default implementation of this method does nothing. Override this
	 * method if you wish to track overflow.
	 */
	public default void markChildOverflow(T object) {
	}

	/**
	 * Called once parsing has finished for this object and its children.
	 * Provides an opportunity to manipulate or replace the parsed value.
	 * 
	 * The default implementation of this method does nothing and returns
	 * the original object. Override this method if you wish to perform any
	 * additional steps
	 */
	public default T finalize(T object) {
		return object;
	}
	
}
