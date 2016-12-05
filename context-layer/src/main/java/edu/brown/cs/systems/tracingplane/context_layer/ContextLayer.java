package edu.brown.cs.systems.tracingplane.context_layer;

import java.nio.ByteBuffer;
import java.util.List;

public interface ContextLayer<B extends ContextBaggage> {
	
	public static final ByteBuffer OVERFLOW_MARKER = ByteBuffer.allocate(0);
	
	/**
	 * Returns true if the provided baggage is an instance of the paramater class B
	 * @param baggage a ContextBaggage instance; possibly null, and possibly not an instance of B
	 * @return true if the baggage is null or if the baggage is an instance of parameter class B; false otherwise.
	 */
	public boolean isInstance(ContextBaggage baggage);
	
	public B newInstance();
	
	public void discard(B baggage);

	public B branch(B from);

	public B join(B left, B right);
	
	public B wrap(List<ByteBuffer> atoms);
	
	public List<ByteBuffer> atoms(B baggage);
	
}