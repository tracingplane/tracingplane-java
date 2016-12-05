package edu.brown.cs.systems.tracingplane.transit_layer.impl;

import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;

public class NullBaggage implements Baggage {

	public static final NullBaggage INSTANCE = new NullBaggage();

	private NullBaggage() {
	}

}
