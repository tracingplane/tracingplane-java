package edu.brown.cs.systems.tracingplane.context_layer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import edu.brown.cs.systems.tracingplane.context_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.context_layer.types.ProtobufVarint;
import edu.brown.cs.systems.tracingplane.transit_layer.Baggage;

public class BaggageImpl implements Baggage {

	static final AtomicIntegerFieldUpdater<ActualBaggageContents> reffer = AtomicIntegerFieldUpdater
			.newUpdater(ActualBaggageContents.class, "refcount");

	static class ActualBaggageContents {
		volatile int refcount = 0;
		List<ByteBuffer> bags;

		public ActualBaggageContents() {
			this(new ArrayList<>());
		}

		public ActualBaggageContents(List<ByteBuffer> bags) {
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

	ActualBaggageContents contents;

	BaggageImpl() {
		this(new ActualBaggageContents());
	}

	BaggageImpl(ActualBaggageContents contents) {
		this.contents = contents;
		this.contents.ref();
	}
	
	BaggageImpl(List<ByteBuffer> bags) {
		this.contents = new ActualBaggageContents(bags);
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

	BaggageImpl branch() {
		return new BaggageImpl(contents);
	}

	BaggageImpl mergeWith(BaggageImpl other) {
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
			this.contents = new ActualBaggageContents(merged);
			return this;
		}
	}

}
