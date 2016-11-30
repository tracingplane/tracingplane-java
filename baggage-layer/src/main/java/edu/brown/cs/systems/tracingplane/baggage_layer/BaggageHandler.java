package edu.brown.cs.systems.tracingplane.baggage_layer;

import java.nio.ByteBuffer;

public interface BaggageHandler {
	
	public BaggageHandler getChildHandler(ByteBuffer field);
	
//	public ApplicationBaggage createBaggageInstance(List<ByteBuffer> data, )

}
