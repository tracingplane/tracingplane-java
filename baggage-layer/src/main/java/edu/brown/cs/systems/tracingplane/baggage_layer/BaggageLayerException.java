package edu.brown.cs.systems.tracingplane.baggage_layer;

public class BaggageLayerException extends Exception {

	private static final long serialVersionUID = -7124041854080833387L;

	private BaggageLayerException(String message) {
		super(message);
	}

	public static class BaggageLayerRuntimeException extends RuntimeException {
		
		private static final long serialVersionUID = 2072227973209173448L;

		public BaggageLayerRuntimeException(String message) {
			super(message);
		}
		
	}
	
	

}
