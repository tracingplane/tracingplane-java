package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.DataPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.IndexedHeaderPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.InlineFieldPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.KeyedHeaderPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BagHeader.IndexedBagHeader;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BagHeader.InlineBagHeader;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BagHeader.KeyedBagHeader;
import edu.brown.cs.systems.tracingplane.context_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.context_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.context_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.context_layer.types.UnsignedLexVarint;

public abstract class Serializer<T> {

	static final Logger log = LoggerFactory.getLogger(Serializer.class);

	/** Used by serializers for writing bags */
	public interface BaggageBuilder {
		/**
		 * Returns a byte buffer with at least the specified amount of space in
		 * it. On the next call to newAtom, the previous bytebuffer is no longer
		 * valid for use. After a bytebuffer is finished with, it is assumed
		 * that the buffer's current position is at the end of its data.
		 * 
		 * eg, if newAtom returns a buffer with position set to i, and its
		 * position is subsequently moved to position j with j >= i, then the
		 * atom is assumed to be all bytes in the range [i, j)
		 */
		public ByteBuffer newAtom(int expectedSize);
	}

	public final List<ByteBuffer> serialize(T instance) {
		SerializerContext ctx = new SerializerContext();
		ctx.serialize(this, instance);
		return ctx.atoms;
	}

	/**
	 * Serializes an instance to the provided serializer
	 */
	protected void serializeTo(T instance, SerializerContext ctx) {
		ctx.serialize(this, instance);
	}

	/**
	 * Serialize the instance's data to the builder (but not children). The
	 * order that data is written does not matter, but it will be sorted
	 * afterwards unless the childOptions specify otherwise
	 */
	protected abstract void serializeAtoms(T instance, BaggageBuilder builder);

	/**
	 * Get all of the fields that should be serialized. The fields can be
	 * returned in any order. The order that they will be serialized might
	 * differ from the order they are returned in. This method can return null,
	 * in which case no children are serialized.
	 */
	protected abstract List<BagHeader> getSerializableChildren(T instance);

	/**
	 * Serialize the data for a child. Typically this means getting a
	 * {@link Serializer} instance for the child, then calling
	 * {@link serializeTo(C instance, SerializerContext ctx)} for the child
	 * serializer.
	 */
	protected abstract void serializeChildAtoms(T instance, BagHeader childField, SerializerContext ctx);

	/**
	 * This method should return true if the data overflowed for the instance
	 */
	protected abstract boolean dataDidOverflow();

	/**
	 * This method should return true if the data overflowed in the children
	 */
	protected abstract boolean childrenDidOverflow();

	private static final class SerializerContext {
		final List<ByteBuffer> atoms;
		final BaggageBuilderImpl builder;

		int currentLevel = -1;
		boolean hasMarkedOverflow = false;

		public SerializerContext() {
			this(1024);
		}

		public SerializerContext(int bufferSize) {
			this.atoms = Lists.newArrayList();
			this.builder = new BaggageBuilderImpl(bufferSize);
		}

		<T> void serialize(Serializer<T> serializer, T instance) {
			currentLevel++;
			serializeData(serializer, instance, DataPrefix.prefix);
			serializeChildren(serializer, instance);
			currentLevel--;
		}

		<T> void serializeData(Serializer<T> serializer, T instance, byte prefix) {
			builder.reset(prefix);
			if (!hasMarkedOverflow && serializer.dataDidOverflow()) {
				hasMarkedOverflow = true;
				atoms.add(BaggageAtoms.OVERFLOW_MARKER);
			}
			int startIndex = atoms.size();
			serializer.serializeAtoms(instance, builder);
			builder.finish();
			int endIndex = atoms.size();
			// TODO: only sort if an option of some kind is specified?
			if (endIndex - startIndex > 1) {
				Collections.sort(atoms.subList(startIndex, endIndex), Lexicographic.BYTE_BUFFER_COMPARATOR);
			}
		}

		<T> void serializeChildren(Serializer<T> serializer, T instance) {
			List<BagHeader> children = serializer.getSerializableChildren(instance);
			Collections.sort(children);

			for (BagHeader child : children) {
				if (child instanceof InlineBagHeader) {
					serializeInlineChild(serializer, instance, (InlineBagHeader) child);
				} else if (child instanceof IndexedBagHeader) {
					serializeIndexedField(serializer, instance, (IndexedBagHeader) child);
				} else if (child instanceof KeyedBagHeader) {
					serializeKeyedField(serializer, instance, (KeyedBagHeader) child);
				}
			}

			if (!hasMarkedOverflow && serializer.childrenDidOverflow()) {
				hasMarkedOverflow = true;
				atoms.add(BaggageAtoms.OVERFLOW_MARKER);
			}
		}

		<T> void serializeInlineChild(Serializer<T> serializer, T instance, InlineBagHeader childField) {
			builder.reset(InlineFieldPrefix.prefixFor(childField.index));
			int startIndex = atoms.size();
			serializer.serializeTo(instance, this);
			builder.finish();
			int endIndex = atoms.size();
			// TODO: only sort if an option of some kind is specified?
			if (endIndex - startIndex > 1) {
				Collections.sort(atoms.subList(startIndex, endIndex), Lexicographic.BYTE_BUFFER_COMPARATOR);
			}
		}

		<T> void serializeIndexedField(Serializer<T> serializer, T instance, IndexedBagHeader childField) {
			builder.writeIndexedHeader(childField, currentLevel);
			int startIndex = atoms.size();
			serializer.serializeTo(instance, this);
			int endIndex = atoms.size();
			// Make sure the child wrote something; if not, then remove the header
			if (endIndex == startIndex) {
				atoms.remove(atoms.size() - 1);
			}
		}

		<T> void serializeKeyedField(Serializer<T> serializer, T instance, KeyedBagHeader childField) {
			builder.writeKeyedHeader(childField, currentLevel);
			int startIndex = atoms.size();
			serializer.serializeTo(instance, this);
			int endIndex = atoms.size();
			// Make sure the child wrote something; if not, then remove the header
			if (endIndex == startIndex) {
				atoms.remove(atoms.size() - 1);
			}
		}

		final class BaggageBuilderImpl implements BaggageBuilder {
			final int backingBufferSize;

			byte currentPrefix;
			ByteBuffer current;
			ByteBuffer backingBuffer;

			BaggageBuilderImpl(int backingBufferSize) {
				this.backingBufferSize = backingBufferSize;
			}

			void reset(byte prefix) {
				finish();
				currentPrefix = prefix;
			}

			void finish() {
				if (current != null) {
					int currentStart = backingBuffer.position();
					int currentEnd = current.position();
					current.position(currentStart);
					current.limit(currentEnd);
					backingBuffer.position(currentEnd);
					atoms.add(current);
					current = null;
				}
			}

			void ensureCapacity(int requiredCapacity) {
				if (backingBuffer == null || backingBuffer.remaining() < requiredCapacity) {
					backingBuffer = ByteBuffer.allocate(Math.max(backingBufferSize, requiredCapacity));
				}
			}

			@Override
			public ByteBuffer newAtom(int expectedSize) {
				ByteBuffer buf = emptyBag(expectedSize + 1);
				buf.put(currentPrefix);
				return buf;
			}

			public ByteBuffer emptyBag(int expectedSize) {
				finish();
				ensureCapacity(expectedSize);
				current = backingBuffer.duplicate();
				return current;
			}

			void writeIndexedHeader(IndexedBagHeader header, int level) {
				final ByteBuffer buf;
				if (header.options != null) {
					buf = emptyBag(10 + header.options.remaining());
				} else {
					buf = emptyBag(10);
				}

				buf.put(IndexedHeaderPrefix.prefixFor(level));
				UnsignedLexVarint.writeLexVarUInt32(buf, header.index);
				ByteBuffers.copyTo(header.options, buf);
				finish();
			}

			void writeKeyedHeader(KeyedBagHeader header, int level) {
				int keyLength = 0;
				if (header.key != null) {
					keyLength = header.key.remaining();
				}
				int serializedSize = 10 + keyLength;
				if (header.options != null) {
					serializedSize += header.options.remaining();
				}
				ByteBuffer buf = emptyBag(serializedSize);

				buf.put(KeyedHeaderPrefix.prefixFor(level));
				UnsignedLexVarint.writeLexVarUInt32(buf, keyLength);
				ByteBuffers.copyTo(header.key, buf);
				ByteBuffers.copyTo(header.options, buf);
				finish();
			}

		}
	}

}
