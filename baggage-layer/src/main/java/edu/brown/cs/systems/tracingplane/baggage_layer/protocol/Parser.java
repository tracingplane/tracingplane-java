package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;
import java.util.Iterator;

import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.atom_layer.types.AtomLayerException;
import edu.brown.cs.systems.tracingplane.atom_layer.types.UnsignedLexVarint;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.AtomPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.DataAtom;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.IndexedHeaderAtom;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.KeyedHeaderAtom;

public abstract class Parser<T> {

	public T parseFrom(Iterator<ByteBuffer> it) throws AtomLayerException {
		return parse(new ParsingContext(it), -1);
	}

	private final T parse(ParsingContext ctx, int currentLevel) throws AtomLayerException {
		T instance = ctx.parseData(this, DataAtom.prefix());
		ctx.parseIndexedChildren(this, instance, currentLevel);
		ctx.parseKeyedChildren(this, instance, currentLevel);
		ctx.finalize(this, instance);
		return instance;
	}

	protected abstract T parseData(Iterator<ByteBuffer> data);

	protected abstract void dataOverflow(T instance);

	protected abstract void childOverflow(T instance);

	protected abstract Parser<?> getParserForChild(int childIndex, ByteBuffer childOptions);

	protected abstract Parser<?> getParserForChild(ByteBuffer childKey, ByteBuffer childOptions);

	protected abstract <C> T setChild(T instance, int childIndex, ByteBuffer childOptions, C childData);

	protected abstract <C> T setChild(T instance, ByteBuffer childKey, ByteBuffer childOptions, C childData);

	private static final class ParsingContext {

		final Iterator<ByteBuffer> it;
		final DataIterator dataIterator = new DataIterator();

		AtomPrefix prefix = null;
		ByteBuffer currentBag = null;

		boolean overflow = false;

		ParsingContext(Iterator<ByteBuffer> baggageIterator) {
			this.it = baggageIterator;
		}

		void advance() {
			while (it.hasNext()) {
				currentBag = it.next();
				if (BaggageAtoms.OVERFLOW_MARKER.equals(currentBag)) {
					overflow = true;
				} else {
					prefix = AtomPrefixes.get(currentBag.get());
					return;
				}
			}
			currentBag = null;
			prefix = null;
		}

		<T> T parseData(Parser<T> parser, AtomPrefix expectedPrefix) {
			dataIterator.reset(expectedPrefix);
			T instance = parser.parseData(dataIterator);
			dataIterator.drainRemaining();
			if (overflow) {
				parser.dataOverflow(instance);
			}
			return instance;
		}

		<T> void parseIndexedChildren(Parser<T> parser, T instance, int currentLevel) throws AtomLayerException {
			while (prefix != null) {
				if (prefix instanceof IndexedHeaderAtom) {
					IndexedHeaderAtom childHeader = (IndexedHeaderAtom) prefix;
					if (childHeader.level > currentLevel) {
						int childIndex = UnsignedLexVarint.readLexVarUInt32(currentBag);
						ByteBuffer childOptions = currentBag;
						Parser<?> childParser = parser.getParserForChild(childIndex, childOptions);
						advance();
						parseIndexedChild(parser, childParser, instance, childIndex, childOptions, childHeader.level);
						continue;
					}
				}
				return;
			}
		}

		<P, C> void parseIndexedChild(Parser<P> parentParser, Parser<C> childParser, P parent, int childIndex,
				ByteBuffer childOptions, int childLevel) throws AtomLayerException {
			if (childParser != null) {
				C child = childParser.parse(this, childLevel);
				if (child != null) {
					parentParser.setChild(parent, childIndex, childOptions, child);
				}
			}
		}

		<T> void parseKeyedChildren(Parser<T> parser, T instance, int currentLevel) throws AtomLayerException {
			while (prefix != null) {
				if (prefix instanceof KeyedHeaderAtom) {
					KeyedHeaderAtom childHeader = (KeyedHeaderAtom) prefix;
					if (childHeader.level > currentLevel) {
						// TODO: what to do when exception thrown here
						int childKeyLength = UnsignedLexVarint.readLexVarUInt32(currentBag);
						ByteBuffer childKey = currentBag.duplicate();
						childKey.limit(childKey.position() + childKeyLength);
						ByteBuffer childOptions = currentBag;
						childOptions.position(childKey.limit());
						Parser<?> childParser = parser.getParserForChild(childKey, childOptions);
						advance();
						parseKeyedChild(parser, childParser, instance, childKey, childOptions, childHeader.level);
						continue;
					}
				}
				return;
			}
		}

		<P, C> void parseKeyedChild(Parser<P> parentParser, Parser<C> childParser, P parent, ByteBuffer childKey,
				ByteBuffer childOptions, int childLevel) throws AtomLayerException {
			if (childParser != null) {
				C child = childParser.parse(this, childLevel);
				if (child != null) {
					parentParser.setChild(parent, childKey, childOptions, child);
				}
			}
		}
		
		<T> void finalize(Parser<T> parser, T instance) {
			if (overflow) {
				parser.childOverflow(instance);
			}
		}

		final class DataIterator implements Iterator<ByteBuffer> {
			AtomPrefix expectedPrefix;

			void reset(AtomPrefix expectedPrefix) {
				this.expectedPrefix = expectedPrefix;
			}

			void drainRemaining() {
				while (hasNext())
					next();
			}

			public boolean hasNext() {
				return prefix == expectedPrefix && currentBag != null;
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
