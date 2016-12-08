package edu.brown.cs.systems.tracingplane.transit_layer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// TODO: description and method documentation
public interface TransitLayer<B extends Baggage> {

    public boolean isInstance(Baggage baggage);

    public B newInstance();

    public void discard(B baggage);

    public B branch(B from);

    public B join(B left, B right);

    public B deserialize(byte[] serialized, int offset, int length);

    public B readFrom(InputStream in) throws IOException;

    public byte[] serialize(B baggage);

    public void writeTo(OutputStream out, B baggage) throws IOException;

}
