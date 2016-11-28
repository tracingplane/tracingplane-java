package edu.brown.cs.systems.tracingplane.context_layer.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import edu.brown.cs.systems.tracingplane.context_layer.ContextBaggage;
import edu.brown.cs.systems.tracingplane.context_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.context_layer.types.ProtobufVarint;

public class BlindContextBaggage implements ContextBaggage {

	static final AtomicIntegerFieldUpdater<SimpleBaggageContents> reffer = AtomicIntegerFieldUpdater
			.newUpdater(SimpleBaggageContents.class, "refcount");

	static class SimpleBaggageContents{
		volatile int refcount = 0;
		List<ByteBuffer> bags;

		public SimpleBaggageContents() {
			this(new ArrayList<>());
		}

		public SimpleBaggageContents(List<ByteBuffer> bags) {
			this.bags = bags;
		}

		void ref() {
			reffer.incrementAndGet(this);
		}

		void deref() {
			if (reffer.decrementAndGet(this) == 0) {
				bags = null;
			}
		}

		boolean editableInPlace() {
			return refcount == 1;
		}
	}

	static final byte[] EMPTY_BYTES = new byte[0];

	SimpleBaggageContents contents;

	BlindContextBaggage() {
		this(new SimpleBaggageContents());
	}
	
	BlindContextBaggage(List<ByteBuffer> bags) {
		this(new SimpleBaggageContents(bags));
	}

	BlindContextBaggage(SimpleBaggageContents contents) {
		this.contents = contents;
		this.contents.ref();
	}

	void discard() {
		this.contents.deref();
		this.contents = null;
	}

	int serializedSize() {
		int size = 0;
		for (ByteBuffer bag : contents.bags) {
			size += bag.remaining() + ProtobufVarint.sizeOf(bag.remaining());
		}
		return size;
	}

	BlindContextBaggage branch() {
		return new BlindContextBaggage(contents);
	}

	BlindContextBaggage mergeWith(BlindContextBaggage other) {
		if (other == null) {
			return this;
		}
		if (contents.editableInPlace()) {
			contents.bags = Lexicographic.merge(contents.bags, other.contents.bags);
			other.contents.deref();
			return this;
		} else if (other.contents.editableInPlace()) {
			return other.mergeWith(this);
		} else {
			List<ByteBuffer> merged = Lexicographic.merge(contents.bags, other.contents.bags);
			this.contents.deref();
			other.contents.deref();
			this.contents = new SimpleBaggageContents(merged);
			return this;
		}
	}

}
