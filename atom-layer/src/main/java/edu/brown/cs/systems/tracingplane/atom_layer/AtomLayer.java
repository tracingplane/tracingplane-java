package edu.brown.cs.systems.tracingplane.atom_layer;

import java.nio.ByteBuffer;
import java.util.List;

public interface AtomLayer<B extends BaggageAtoms> {
	
	/**
	 * Returns true if the provided baggage is an instance of the paramater class B
	 * @param baggage a ContextBaggage instance; possibly null, and possibly not an instance of B
	 * @return true if the baggage is null or if the baggage is an instance of parameter class B; false otherwise.
	 */
	public boolean isInstance(BaggageAtoms baggage);
	
	public B newInstance();
	
	public void discard(B baggage);

	public B branch(B from);

	public B join(B left, B right);
	
	public B wrap(List<ByteBuffer> atoms);
	
	public List<ByteBuffer> atoms(B baggage);
	
}