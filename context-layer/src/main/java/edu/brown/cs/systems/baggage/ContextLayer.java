package edu.brown.cs.systems.baggage;

import java.io.InputStream;
import java.io.OutputStream;

public class ContextLayer implements TransitLayer {

	@Override
	public Baggage newInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Baggage branch(Baggage from) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Baggage join(Baggage left, Baggage right) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Baggage deserialize(byte[] serialized, int offset, int length) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Baggage readFrom(InputStream in) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] serialize(Baggage instance) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeTo(Baggage instance, OutputStream out) {
		// TODO Auto-generated method stub
		
	}

}
