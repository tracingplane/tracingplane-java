package edu.brown.cs.systems.tracingplane.transit_layer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface TransitLayer2<B extends Baggage2> {

	public boolean isInstance(Baggage2 baggage);

	public B newInstance();

	public void discard(B baggage);

	public B branch(B from);

	public B join(B left, B right);

	public B deserialize(byte[] serialized, int offset, int length);

	public B readFrom(InputStream in) throws IOException;

	public byte[] serialize(B baggage);

	public void writeTo(OutputStream out, B baggage) throws IOException;

}
