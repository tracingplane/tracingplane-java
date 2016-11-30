package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.systems.tracingplane.baggage_layer.impl.Bag.ChildElement;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.ElementType.FieldData;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.ElementType.IndexedField;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.ElementType.InlineFieldData;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.ElementType.NamedField;
import edu.brown.cs.systems.tracingplane.context_layer.ContextLayer;

public class BagParser {

	private final int baggageDataCount;
	private final List<ByteBuffer> baggageData;
	private int nextBagIndex = 0;
	private byte firstByte;
	private ByteBuffer currentBag = null;

	private boolean overflow = false;

	private BagParser(List<ByteBuffer> baggageData) {
		this.baggageData = baggageData;
		this.baggageDataCount = baggageData.size();
	}

	private void advance() {
		for (; nextBagIndex < baggageDataCount; nextBagIndex++) {
			currentBag = baggageData.get(nextBagIndex);
			if (ContextLayer.OVERFLOW_MARKER.equals(currentBag)) {
				overflow = true;
			} else {
				firstByte = currentBag.get();
				return;
			}
		}
		currentBag = null;
	}
	
	public Bag parse(List<ByteBuffer> baggage) {
		return new BagParser(baggage).readBag(-1);
	}

	private Bag readBag(int currentLevel) {
		advance();

		List<ByteBuffer> data = readDataElements(FieldData.byteValue);
		boolean dataOverflowed = overflow;

		List<ChildElement> indexed = null;
		List<ChildElement> named = null;

		indexed = readInlinedBags(indexed);
		indexed = readIndexedBags(currentLevel, indexed);
		named = readNamedBags(currentLevel, named);
		boolean bagOverflowed = overflow;

		return new Bag(dataOverflowed, bagOverflowed, data, indexed, named);
	}

	private List<ByteBuffer> readDataElements(final byte expectedPrefix) {
		if (firstByte != expectedPrefix || currentBag == null) {
			return null;
		}

		List<ByteBuffer> dataBags = new ArrayList<>();
		for (; firstByte == expectedPrefix && currentBag != null; advance()) {
			dataBags.add(currentBag);
		}

		return dataBags;
	}

	private List<ChildElement> readInlinedBags(List<ChildElement> children) {
		if (!InlineFieldData.isInlineData(firstByte) || currentBag == null) {
			return null;
		}

		if (children == null) {
			children = new ArrayList<>();
		}
		for (; InlineFieldData.isInlineData(firstByte) && currentBag != null; advance()) {
			ByteBuffer childId = ByteBuffer.wrap(new byte[] { firstByte });
			Bag bag = makeBag(overflow, overflow, readDataElements(firstByte), null, null);
			children.add(new ChildElement(childId, bag));
		}

		return children;
	}

	private List<ChildElement> readIndexedBags(final int currentLevel, List<ChildElement> children) {
		if (IndexedField.level(firstByte) <= currentLevel || currentBag == null) {
			return children;
		}

		int childLevel;
		if (children == null) {
			children = new ArrayList<>();
		}
		for (; (childLevel = IndexedField.level(firstByte)) > currentLevel && currentBag != null; advance()) {
			ByteBuffer childId = currentBag;
			children.add(new ChildElement(childId, readBag(childLevel)));
		}

		return children;
	}

	private List<ChildElement> readNamedBags(final int currentLevel, List<ChildElement> children) {
		if (NamedField.level(firstByte) <= currentLevel || currentBag == null) {
			return children;
		}

		int childLevel;
		if (children == null) {
			children = new ArrayList<>();
		}
		for (; (childLevel = NamedField.level(firstByte)) > currentLevel && currentBag != null; advance()) {
			ByteBuffer childId = currentBag;
			children.add(new ChildElement(childId, readBag(childLevel)));
		}

		return children;
	}

	private Bag makeBag(boolean dataOverflow, boolean bagOverflow, List<ByteBuffer> data,
			List<ChildElement> indexedChildren, List<ChildElement> namedChildren) {
		return new Bag(dataOverflow, bagOverflow, data, indexedChildren, namedChildren);
	}

}
