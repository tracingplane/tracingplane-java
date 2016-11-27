package edu.brown.cs.systems.tracingplane.transit_layer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface TransitLayer {

	public Baggage newInstance();
	
	public Baggage branch(Baggage from);

	public Baggage join(Baggage left, Baggage right);
	
	public Baggage deserialize(byte[] serialized, int offset, int length);
	
	public Baggage readFrom(InputStream in) throws IOException;
	
	public byte[] serialize(Baggage instance);
	
	public void writeTo(OutputStream out, Baggage instance) throws IOException;

}
