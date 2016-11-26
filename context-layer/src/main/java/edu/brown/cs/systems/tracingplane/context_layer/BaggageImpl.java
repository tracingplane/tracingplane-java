package edu.brown.cs.systems.tracingplane.context_layer;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.systems.tracingplane.context_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;

public class BaggageImpl implements Baggage {
	
	static final byte[] EMPTY_BYTES = new byte[0];
	
	public List<ByteBuffer> bags;
	
	public BaggageImpl() {
		this(new ArrayList<>());
	}
	
	public BaggageImpl(List<ByteBuffer> bags) {
		this.bags = bags;
	}
	
	BaggageImpl branch() {
		return new BaggageImpl(new ArrayList<>(bags));
	}
	
	BaggageImpl mergeWith(BaggageImpl other) {
		if (other != null) {
			bags = Lexicographic.merge(bags, other.bags);
		}
		return this;
	}
	
	static BaggageImpl deserialize(byte[] bytes, int offset, int length) {
		return null;
	}
	
	static BaggageImpl readFrom(InputStream in) {
		return null;
	}
	
	byte[] serialize() {
		return null;
	}
	
	void writeTo(OutputStream out) {
		
	}

}
