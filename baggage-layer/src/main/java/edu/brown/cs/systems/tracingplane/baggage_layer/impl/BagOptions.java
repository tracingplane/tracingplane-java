package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

public class BagOptions implements Comparable<BagOptions> {
	
	public static final BagOptions DEFAULT_OPTIONS = new BagOptions();
	
	public BagOptions() {
	}
	
	@Override
	public String toString() {
		return "BagOptions";
	}
	
	@Override
	public boolean equals(Object other) {
		if (other != null && other instanceof BagOptions) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(BagOptions o) {
		if (o == null) {
			throw new NullPointerException();
		} else {
			return 0;
		}
	}

}
