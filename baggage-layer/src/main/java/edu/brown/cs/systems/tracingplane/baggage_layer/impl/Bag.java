package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public class Bag implements Comparable<Bag> {
	
	public int depth;
	public List<ByteBuffer> data = null;
	public Map<Long, Bag> elements;
	public Map<ByteBuffer, Bag> namedChildren;
	
	

	@Override
	public int compareTo(Bag o) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}