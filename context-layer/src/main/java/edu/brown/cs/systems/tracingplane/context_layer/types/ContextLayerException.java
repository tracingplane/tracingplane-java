package edu.brown.cs.systems.tracingplane.context_layer.types;

import java.nio.BufferUnderflowException;

public class ContextLayerException extends Exception {
	
	private static final long serialVersionUID = -658006356579507967L;

	public ContextLayerException(String message) {
		super(message);
	}

	public ContextLayerException(String message, BufferUnderflowException e) {
		super(message, e);
	}

}
