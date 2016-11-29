package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

public enum BagCode {
	
	Data(0), OverflowMarker(1), FieldHeader(2);
	
	public final int id;
	
	private BagCode(int id) {
		this.id = id;
	}

}
