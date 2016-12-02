package edu.brown.cs.systems.tracingplane.baggage_layer.impl_old;

import java.nio.ByteBuffer;
import java.util.List;

public class Bag implements Comparable<Bag> {

	public static class ChildElement {
		public final ByteBuffer id;
		public final Bag bag;

		public ChildElement(ByteBuffer id, Bag bag) {
			this.id = id;
			this.bag = bag;
		}
	}

	public boolean dataOverflow; // did the direct data of this bag overflow?
	public boolean bagOverflow; // did overflow occur somewhere in this bag or
								// bag's children
	public List<ByteBuffer> data;
	public List<ChildElement> indexedChildren;
	public List<ChildElement> namedChildren;

	public Bag(boolean dataOverflow, boolean bagOverflow, List<ByteBuffer> data, List<ChildElement> indexedChildren,
			List<ChildElement> namedChildren) {
		this.dataOverflow = dataOverflow;
		this.bagOverflow = bagOverflow;
		this.data = data;
		this.indexedChildren = indexedChildren;
		this.namedChildren = namedChildren;
	}

	@Override
	public int compareTo(Bag o) {
		return 0;
	}

}