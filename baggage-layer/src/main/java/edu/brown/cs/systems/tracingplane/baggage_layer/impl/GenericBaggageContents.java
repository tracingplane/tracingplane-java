package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageContents;

public class GenericBaggageContents implements BaggageContents {
	
	public boolean dataDidOverflow = false;
	public boolean dataWasTrimmed = false;
	public boolean childDidOverflow = false;
	public boolean childWasTrimmed = false;
	public List<ByteBuffer> data = null;
	public Map<BagKey, GenericBaggageContents> children = null;
	

	public GenericBaggageContents branch() {
		// TODO Auto-generated method stub
		return null;
	}

	public GenericBaggageContents mergeWith(GenericBaggageContents other) {
		// TODO Auto-generated method stub
		return null;
	}

}
