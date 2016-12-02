package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import java.nio.ByteBuffer;
import java.util.Iterator;

import edu.brown.cs.systems.tracingplane.baggage_layer.impl.AtomPrefixes.DataPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.AtomPrefixes.IndexedBagHeaderPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.AtomPrefixes.InlineFieldPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.AtomPrefixes.KeyedBagHeaderPrefix;
import edu.brown.cs.systems.tracingplane.context_layer.ContextLayer;
import edu.brown.cs.systems.tracingplane.context_layer.types.ContextLayerException;
import edu.brown.cs.systems.tracingplane.context_layer.types.UnsignedLexVarint;

public abstract class Parser2<T> {

	public T parseFrom(Iterator<ByteBuffer> it) throws ContextLayerException {
		return parse(new ParsingContext(it), -1);
	}

	private final T parse(ParsingContext ctx, int currentLevel) throws ContextLayerException {
		T instance = ctx.parseData(this, DataPrefix.prefix);
		ctx.parseInlineChildren(this, instance);
		ctx.parseIndexedChildren(this, instance, currentLevel);
		ctx.parseKeyedChildren(this, instance, currentLevel);
		ctx.finalize(this, instance);
		return instance;
	}

	private final T parseInline(ParsingContext ctx, byte prefix) {
		return ctx.parseData(this, prefix);
	}

	protected abstract T parseData(Iterator<ByteBuffer> data);

	protected abstract void dataOverflow(T instance);

	protected abstract void childOverflow(T instance);

	protected abstract Parser2<?> getParserForChild(int childIndex, ByteBuffer childOptions);

	protected abstract Parser2<?> getParserForChild(ByteBuffer childKey, ByteBuffer childOptions);

	protected abstract <C> T setChild(T instance, int childIndex, ByteBuffer childOptions, C childData);

	protected abstract <C> T setChild(T instance, ByteBuffer childKey, ByteBuffer childOptions, C childData);

	private static final class ParsingContext {

		final Iterator<ByteBuffer> it;
		final DataIterator dataIterator = new DataIterator();

		byte firstByte;
		ByteBuffer currentBag = null;

		boolean overflow = false;

		ParsingContext(Iterator<ByteBuffer> baggageIterator) {
			this.it = baggageIterator;
		}

		void advance() {
			while (it.hasNext()) {
				currentBag = it.next();
				if (ContextLayer.OVERFLOW_MARKER.equals(currentBag)) {
					overflow = true;
				} else {
					firstByte = currentBag.get();
					return;
				}
			}
			currentBag = null;
		}

		<T> T parseData(Parser2<T> parser, byte expectedPrefix) {
			dataIterator.reset(expectedPrefix);
			T instance = parser.parseData(dataIterator);
			dataIterator.drainRemaining();
			if (overflow) {
				parser.dataOverflow(instance);
			}
			return instance;
		}

		<T> void parseInlineChildren(Parser2<T> parser, T instance) {
			while (InlineFieldPrefix.isInlineData(firstByte) && currentBag != null) {
				byte childIndex = firstByte;
				Parser2<?> childParser = parser.getParserForChild(childIndex, null);
				parseInlineChild(parser, childParser, instance, childIndex);
			}
		}

		<P, C> void parseInlineChild(Parser2<P> parentParser, Parser2<C> childParser, P parent, byte childIndex) {
			if (childParser != null) {
				C child = childParser.parseInline(this, childIndex);
				if (child != null) {
					parentParser.setChild(parent, childIndex, null, child);
				}
			}
		}

		<T> void parseIndexedChildren(Parser2<T> parser, T instance, int currentLevel) throws ContextLayerException {
			int childLevel;
			while ((childLevel = IndexedBagHeaderPrefix.level(firstByte)) > currentLevel && currentBag != null) {
				// TODO: what to do when exception thrown here
				int childIndex = UnsignedLexVarint.readLexVarUInt32(currentBag);
				ByteBuffer childOptions = currentBag;
				Parser2<?> childParser = parser.getParserForChild(childIndex, childOptions);
				advance();
				parseIndexedChild(parser, childParser, instance, childIndex, childOptions, childLevel);
			}
		}

		<P, C> void parseIndexedChild(Parser2<P> parentParser, Parser2<C> childParser, P parent, int childIndex,
				ByteBuffer childOptions, int childLevel) throws ContextLayerException {
			if (childParser != null) {
				C child = childParser.parse(this, childLevel);
				if (child != null) {
					parentParser.setChild(parent, childIndex, childOptions, child);
				}
			}
		}

		<T> void parseKeyedChildren(Parser2<T> parser, T instance, int currentLevel) throws ContextLayerException {
			int childLevel;
			while ((childLevel = KeyedBagHeaderPrefix.level(firstByte)) > currentLevel && currentBag != null) {
				// TODO: what to do when exception thrown here
				int childKeyLength = UnsignedLexVarint.readLexVarUInt32(currentBag);
				ByteBuffer childKey = currentBag.duplicate();
				childKey.limit(childKey.position() + childKeyLength);
				ByteBuffer childOptions = currentBag;
				childOptions.position(childKey.limit());
				Parser2<?> childParser = parser.getParserForChild(childKey, childOptions);
				advance();
				parseKeyedChild(parser, childParser, instance, childKey, childOptions, childLevel);
			}
		}

		<P, C> void parseKeyedChild(Parser2<P> parentParser, Parser2<C> childParser, P parent, ByteBuffer childKey,
				ByteBuffer childOptions, int childLevel) throws ContextLayerException {
			if (childParser != null) {
				C child = childParser.parse(this, childLevel);
				if (child != null) {
					parentParser.setChild(parent, childKey, childOptions, child);
				}
			}
		}
		
		<T> void finalize(Parser2<T> parser, T instance) {
			if (overflow) {
				parser.childOverflow(instance);
			}
		}

		final class DataIterator implements Iterator<ByteBuffer> {
			byte expectedPrefix;

			void reset(byte expectedPrefix) {
				this.expectedPrefix = expectedPrefix;
			}

			void drainRemaining() {
				while (hasNext())
					next();
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

}
