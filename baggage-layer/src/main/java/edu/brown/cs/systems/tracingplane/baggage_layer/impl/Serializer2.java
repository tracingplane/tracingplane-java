package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.brown.cs.systems.tracingplane.baggage_layer.impl.AtomPrefixes.DataPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.AtomPrefixes.InlineFieldPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.BagHeader.IndexedBagHeader;
import edu.brown.cs.systems.tracingplane.baggage_layer.impl.BagHeader.InlineBagHeader;
import edu.brown.cs.systems.tracingplane.context_layer.ContextLayer;
import edu.brown.cs.systems.tracingplane.context_layer.types.Lexicographic;

public abstract class Serializer2<T> {

	static final Logger log = LoggerFactory.getLogger(Serializer2.class);

	/** Used by serializers for writing bags */
	public interface BaggageBuilder {
		/**
		 * Returns a byte buffer with at least the specified amount of space in
		 * it. On the next call to allocateNewBag, the previous bytebuffer is no
		 * longer valid for use. After a byte buffer is finished with, it is
		 * assumed that the buffer's current position is at the end of its data.
		 * 
		 * eg, if newBag returns a buffer with position set to i, and its
		 * position is subsequently moved to position j with j >= i, then the
		 * baggage contents are assumed to be all bytes in the range [i, j)
		 */
		public ByteBuffer newBag(int expectedSize);
	}

	public final List<ByteBuffer> serialize(T instance) {
		SerializerContext ctx = new SerializerContext();
		serialize(instance, ctx, -1);
		return ctx.data;
	}

	private final void serialize(T instance, SerializerContext ctx, int currentLevel) {
		ctx.serializeData(this, instance, DataPrefix.prefix);
		ctx.serializeChildren(this, instance, currentLevel);
	}

	/**
	 * Serialize the instance's data to the builder (but not children). The
	 * order that data is written does not matter, but it will be sorted
	 * afterwards unless the childOptions specify otherwise
	 */
	protected abstract void serializeData(T instance, BaggageBuilder builder);

	/**
	 * Get all of the fields that should be serialized. The fields can be
	 * returned in any order. The order that they will be serialized might
	 * differ from the order they are returned in.
	 */
	protected abstract List<BagHeader> getSerializableChildren(T instance);

	/**
	 * Serialize the data for a child to the provided builder. Typically this
	 * means just getting the serializer for the child then calling
	 * {@link serializeData(T instance, BaggageBuilder builder)}
	 */
	protected abstract void serializeChildData(T instance, BagHeader childField, BaggageBuilder builder);

	protected abstract boolean dataDidOverflow();

	protected abstract boolean childrenDidOverflow();

	private static final class SerializerContext {
		final List<ByteBuffer> data;
		final BaggageBuilderImpl builder;

		boolean hasMarkedOverflow = false;
		
		public SerializerContext() {
			this(1024);
		}
		
		public SerializerContext(int bufferSize) {
			this.data = Lists.newArrayList();
			this.builder = new BaggageBuilderImpl(bufferSize);
		}

		<T> void serializeData(Serializer2<T> serializer, T instance, byte prefix) {
			builder.reset(prefix);
			if (!hasMarkedOverflow && serializer.dataDidOverflow()) {
				hasMarkedOverflow = true;
				data.add(ContextLayer.OVERFLOW_MARKER);
			}
			int startIndex = data.size();
			serializer.serializeData(instance, builder);
			builder.finish();
			int endIndex = data.size();
			// TODO: only sort if an option of some kind is specified?
			if (endIndex - startIndex > 1) {
				Collections.sort(data.subList(startIndex, endIndex), Lexicographic.BYTE_BUFFER_COMPARATOR);
			}
		}

		<T> void serializeChildren(Serializer2<T> serializer, T instance, int currentLevel) {
			List<BagHeader> children = serializer.getSerializableChildren(instance);
			Collections.sort(children);
			
			for (BagHeader child : children) {
				if (child instanceof InlineBagHeader) {
					serializeInlineChild(serializer, instance, (InlineBagHeader) child);
				} else if (child instanceof IndexedBagHeader) {
					serializeIndexedField(serializer, instance, (IndexedBagHeader) child, currentLevel + 1);
				}
			}

		}
		
		<T> void serializeInlineChild(Serializer2<T> serializer, T instance, InlineBagHeader childField) {
			builder.reset(InlineFieldPrefix.prefixFor(childField.index));
			int startIndex = data.size();
			serializer.serializeChildData(instance, childField, builder);
			builder.finish();
			int endIndex = data.size();
			// TODO: only sort if an option of some kind is specified?
			if (endIndex - startIndex > 1) {
				Collections.sort(data.subList(startIndex, endIndex), Lexicographic.BYTE_BUFFER_COMPARATOR);
			}
		}
		
		<T> void serializeIndexedField(Serializer2<T> serializer, T instance, IndexedBagHeader childField, int childLevel) {
//			ByteBuffer fieldHeader = builder.emptyBag(1 + UnsignedLexVarint.encodedLength(childField.index) + childField.options.remaining());
//			if (childField.options != null && childField.options.remaining() > 0) {
//				
//			} else {
//				ByteBuffer fieldHeader = builder.emptyBag(1);
//				fieldHeader.put(b)
//				builder.newBag(0);
//				builder.finish();
//			}
//			builder.newBag(childField.options == null ? 0 : childField.options.remaining());
			
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
					data.add(current);
					current = null;
				}
			}

			void ensureCapacity(int requiredCapacity) {
				if (backingBuffer == null || backingBuffer.remaining() < requiredCapacity) {
					backingBuffer = ByteBuffer.allocate(Math.max(backingBufferSize, requiredCapacity));
				}
			}

			@Override
			public ByteBuffer newBag(int expectedSize) {
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

		}
	}

}
