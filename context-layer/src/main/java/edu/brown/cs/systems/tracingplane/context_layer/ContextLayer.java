package edu.brown.cs.systems.tracingplane.context_layer;

import java.nio.ByteBuffer;
import java.util.List;

public interface ContextLayer {
	
	public static final ByteBuffer OVERFLOW_MARKER = ByteBuffer.allocate(0);
	
	public void discard(ContextBaggage baggage);

	public ContextBaggage branch(ContextBaggage from);

	public ContextBaggage join(ContextBaggage left, ContextBaggage right);
	
	public ContextBaggage wrap(List<ByteBuffer> bags);
	
	public List<ByteBuffer> bags(ContextBaggage baggage);
	
}