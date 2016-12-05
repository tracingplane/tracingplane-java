package edu.brown.cs.systems.tracingplane.baggage_layer;

import java.nio.ByteBuffer;
import java.util.List;

import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.Parser;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.Serializer;

public interface BaggageLayer<T> {
	
	public Parser<T> parser();
	
	public Serializer<T> serializer();
	
	public void discard(ApplicationBaggage baggage);

	public ApplicationBaggage branch(ApplicationBaggage baggage);

	public ApplicationBaggage join(ApplicationBaggage left, ApplicationBaggage right);
	
	public ApplicationBaggage wrap(List<ByteBuffer> atoms);
	
	public List<ByteBuffer> atoms(ApplicationBaggage baggage);

}
