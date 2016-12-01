package edu.brown.cs.systems.tracingplane.baggage_layer.prototype.baggage_buffers.library;

import java.nio.ByteBuffer;

import edu.brown.cs.systems.tracingplane.baggage_layer.prototype2.BagParser;
import edu.brown.cs.systems.tracingplane.baggage_layer.prototype2.BagSerializer;
import edu.brown.cs.systems.tracingplane.baggage_layer.prototype2.BagSerializer.ChildBuilder;

public interface Field<ParentType, ElementType> {
	
	public BagParser<ElementType> parser();
	
	public BagSerializer<ElementType> serializer();
	
	public void set(ParentType parent, ElementType element);
	
	public ElementType getFrom(ParentType parent);
	
	@SuppressWarnings("unchecked")
	public default void setCast(ParentType parent, Object element) {
		set(parent, (ElementType) element);
	}
	
	public default void serializeTo(ChildBuilder builder, int index, ByteBuffer childOptions, ParentType parent) {
		ElementType element = getFrom(parent);
		if (element != null) {
			builder.addIndexedChild(index, childOptions, serializer(), element);
		}
	}

}
