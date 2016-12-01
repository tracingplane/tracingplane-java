package edu.brown.cs.systems.tracingplane.baggage_layer.prototype2;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import edu.brown.cs.systems.tracingplane.baggage_layer.impl.ElementType.FieldData;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.ElementType.IndexedField;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.ElementType.InlineFieldData;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.ElementType.NamedField;
import edu.brown.cs.systems.tracingplane.context_layer.ContextLayer;
import edu.brown.cs.systems.tracingplane.context_layer.types.ContextLayerException;
import edu.brown.cs.systems.tracingplane.context_layer.types.UnsignedLexVarint;

public class Parser<T> {

	private final int baggageDataCount;
	private final List<ByteBuffer> baggageData;
	private int nextBagIndex = 0;
	private byte firstByte;
	private ByteBuffer currentBag = null;

	private boolean overflow = false;
	
	private Parser(List<ByteBuffer> baggageData) {
		this.baggageData = baggageData;
		this.baggageDataCount = baggageData.size();
	}
	
	public static <T> T parse(List<ByteBuffer> allBags, BagParser<T> parser) throws ContextLayerException {
		return new Parser<T>(allBags).parse(parser);
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
	
	public T parse(BagParser<T> parser) throws ContextLayerException {
		return parseFullChild(parser, -1);
	}
	
	private final DataIterator dataIterator = new DataIterator();
	
	public <C> C parseFullChild(BagParser<C> parser, int currentLevel) throws ContextLayerException {
		if (parser == null) {
			return skipFullBag(currentLevel + 1);
		}
		
		C instance = parseData(parser);
		
		parseInlineChildren(parser, instance);
		parseIndexedChildren(parser, instance, currentLevel);
		parseKeyedChildren(parser, instance, currentLevel);
		
		if (overflow) {
			parser.markChildOverflow(instance);
		}
		
		parser.finalize(instance);
		
		return instance;
	}
	
	private <C> C parseInlineChild(BagParser<C> parser, byte expectedFirstByte) {
		if (parser == null) {
			return skipInlineBag(expectedFirstByte);
		}
		
		dataIterator.reset(expectedFirstByte);
		C child = parser.parse(dataIterator);
		if (overflow) {
			parser.markChildOverflow(child);
		}
		parser.finalize(child);
		return child;
	}
	
	private <C> C parseData(BagParser<C> parser) {
		dataIterator.reset(FieldData.byteValue);
		C instance = parser.parse(dataIterator);
		if (overflow) {
			parser.markDataOverflow(instance);
		}
		return instance;
	}
	
	private <C> void parseInlineChildren(BagParser<C> parentParser, C parent) {
		while (InlineFieldData.isInlineData(firstByte) && currentBag != null) {
			byte childIndex = firstByte;
			BagParser<?> childParser = parentParser.getParserForChild(childIndex, null);
			
			Object child = parseInlineChild(childParser, childIndex);
			if (child != null) {
				parentParser.setChild(childIndex, null, parent, child);
			}
		}
	}
	
	private <C> void parseIndexedChildren(BagParser<C> parentParser, C parent, int parentLevel) throws ContextLayerException {
		int childLevel;
		while ((childLevel = IndexedField.level(firstByte)) > parentLevel && currentBag != null) {
			int childIndex = UnsignedLexVarint.readLexVarUInt32(currentBag); // TODO: what to do when exception thrown here
			ByteBuffer childOptions = currentBag;
			BagParser<?> childParser = parentParser.getParserForChild(childIndex, childOptions);
			advance();
			
			Object child = parseFullChild(childParser, childLevel);
			if (child != null) {
				parentParser.setChild(childIndex, childOptions, parent, child);
			}
		}
	}
	
	private <C> void parseKeyedChildren(BagParser<C> parentParser, C parent, int parentLevel) throws ContextLayerException {
		int childLevel;
		while ((childLevel = NamedField.level(firstByte)) > parentLevel && currentBag != null) {
			int childKeyLength = UnsignedLexVarint.readLexVarUInt32(currentBag);
			ByteBuffer childKey = currentBag.duplicate();
			childKey.limit(childKey.position() + childKeyLength);
			ByteBuffer childOptions = currentBag;
			childOptions.position(childKey.limit());
			BagParser<?> childParser = parentParser.getParserForChild(childKey, childOptions);
			advance();
			
			Object child = parseFullChild(childParser, childLevel);
			if (child != null) {
				parentParser.setChild(childKey, childOptions, parent, child);
			}
		}
	}
	
	/** Skips all bags at or above the specified level */
	private <C> C skipFullBag(int level) {
		while (currentBag != null) {
			if (IndexedField.bagType.match(firstByte) && IndexedField.level(firstByte) < level) {
				break;
			} else if (NamedField.bagType.match(firstByte) && NamedField.level(firstByte) < level) {
				break;
			} else {
				advance();
			}
		}
		return null;
	}
	
	private <C> C skipInlineBag(byte expectedFirstByte) {
		while (currentBag != null && firstByte == expectedFirstByte) {
			advance();
		}
		return null;
	}
	
	private final class DataIterator implements Iterator<ByteBuffer> {
		byte expectedPrefix;
		public void reset(byte expectedPrefix) {
			this.expectedPrefix = expectedPrefix;
		}
		public boolean hasNext() {
			return firstByte == expectedPrefix && currentBag != null;
		}
		public ByteBuffer next() {
			try {
				return currentBag;
			} finally {
				advance();
			}
		}
	}

}
