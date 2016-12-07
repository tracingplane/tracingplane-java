package edu.brown.cs.systems.tracingplane.baggage_layer;

public class BagOptions implements Comparable<BagOptions> {

	public static final BagOptions DEFAULT_OPTIONS = new BagOptions();

	public BagOptions() {
	}

	public boolean canInline() {
		return true; // TODO: allow configuration of this?
	}

	@Override
	public String toString() {
		return "BagOptions";
	}

	@Override
	public int compareTo(BagOptions o) {
		if (o == null) {
			throw new NullPointerException();
		} else {
			return 0;
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		} else if (other == null) {
			return this.equals(DEFAULT_OPTIONS);
		} else if (other instanceof BagOptions) {
			// TODO: once options in place, do checks
			return true;
		} else {
			return false;
		}
	}

}
