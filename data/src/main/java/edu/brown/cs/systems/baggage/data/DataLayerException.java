package edu.brown.cs.systems.baggage.data;

import java.nio.BufferUnderflowException;

public class DataLayerException extends Exception {
	
	private static final long serialVersionUID = -658006356579507967L;

	public DataLayerException(String message) {
		super(message);
	}

	public DataLayerException(String message, BufferUnderflowException e) {
		super(message, e);
	}

}
