package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

public class BaggageSerializationException extends Exception {

	private static final long serialVersionUID = 6154243824569490653L;

	public BaggageSerializationException(String message) {
		super(message);
	}

	public static BaggageSerializationException invalidInlineField(int index) {
		return new BaggageSerializationException(
				String.format("%d is not a valid inline field index, must be in range [%d, %d)", index, 0,
						AtomPrefixes.InlineFieldPrefix.prefixes.length));
	}
	
	

}
