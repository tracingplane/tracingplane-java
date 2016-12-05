package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

public class Bag {

	static final AtomicIntegerFieldUpdater<Bag> reffer = AtomicIntegerFieldUpdater
			.newUpdater(Bag.class, "refcount");
	
	private volatile int refCount = 0;
	
	Bag() {
	}
	
	
	
	

}
