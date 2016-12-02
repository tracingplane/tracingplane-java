package edu.brown.cs.systems.tracingplane.baggage_layer.impl_old3;

import java.nio.ByteBuffer;

public interface BagSerializer<T> {

	public interface BagBuilder {

		public void markOverflow();

		public void addData(ByteBuffer data);

	}

	public interface ChildBuilder {

		public <C> void addIndexedChild(int index, ByteBuffer childOptions, BagSerializer<C> childSerializer,
				C childData);

		public <C> void addKeyedChild(ByteBuffer key, ByteBuffer childOptions, BagSerializer<C> childSerializer,
				C childData);

	}

	/**
	 * Serialize the data of the element and add it to the builder. This
	 * only serializes the direct data, not the children
	 */
	public default void serialize(BagBuilder builder, T element) {
	}

	/**
	 * Serialize the data of the children to the builder
	 */
	public default void serializeChildren(ChildBuilder builder, T element) {
	}

}
