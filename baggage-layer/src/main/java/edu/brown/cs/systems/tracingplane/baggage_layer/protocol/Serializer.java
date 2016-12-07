package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.atom_layer.types.Lexicographic;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.AtomPrefix;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.DataAtom;

// TODO: documentation, description, handling of trim marker, handling of exceptions
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
	protected abstract List<BagKey> getSerializableChildren(T instance);

	/**
	 * Serialize all atoms for the child -- both its data as well as its
	 * subsequent children. Typically this means getting a {@link Serializer}
	 * instance for the child, then calling {@link serializeTo(C instance,
	 * SerializerContext ctx)} for the child serializer.
	 */
	protected abstract void serializeChildAtoms(T instance, BagKey childField, SerializerContext ctx);

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
			serializeData(serializer, instance, DataAtom.prefix());
			serializeChildren(serializer, instance);
			currentLevel--;
		}

		<T> void serializeData(Serializer<T> serializer, T instance, AtomPrefix prefix) {
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
			List<BagKey> children = serializer.getSerializableChildren(instance);
			Collections.sort(children);

			for (BagKey child : children) {
				if (child instanceof BagKey.Indexed) {
					serializeIndexedField(serializer, instance, (BagKey.Indexed) child);
				} else if (child instanceof BagKey.Keyed) {
					serializeKeyedField(serializer, instance, (BagKey.Keyed) child);
				}
			}

			if (!hasMarkedOverflow && serializer.childrenDidOverflow()) {
				hasMarkedOverflow = true;
				atoms.add(BaggageAtoms.OVERFLOW_MARKER);
			}
		}

		<T> void serializeIndexedField(Serializer<T> serializer, T instance, BagKey.Indexed childField) {
			builder.writeIndexedHeader(childField, currentLevel);
			int startIndex = atoms.size();
			serializer.serializeTo(instance, this);
			int endIndex = atoms.size();
			// Make sure the child wrote something; if not, then remove the
			// header
			if (endIndex == startIndex) {
				atoms.remove(atoms.size() - 1);
			}
		}

		<T> void serializeKeyedField(Serializer<T> serializer, T instance, BagKey.Keyed childField) {
			builder.writeKeyedHeader(childField, currentLevel);
			int startIndex = atoms.size();
			serializer.serializeTo(instance, this);
			int endIndex = atoms.size();
			// Make sure the child wrote something; if not, then remove the
			// header
			if (endIndex == startIndex) {
				atoms.remove(atoms.size() - 1);
			}
		}

		final class BaggageBuilderImpl implements BaggageBuilder {
			final int backingBufferSize;

			AtomPrefix currentPrefix;
			ByteBuffer current;
			ByteBuffer backingBuffer;

			BaggageBuilderImpl(int backingBufferSize) {
				this.backingBufferSize = backingBufferSize;
			}

			void reset(AtomPrefix prefix) {
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
				buf.put(currentPrefix.prefix);
				return buf;
			}

			public ByteBuffer emptyBag(int expectedSize) {
				finish();
				ensureCapacity(expectedSize);
				current = backingBuffer.duplicate();
				return current;
			}

			void writeIndexedHeader(BagKey.Indexed field, int level) {
				ByteBuffer buf = emptyBag(10 + BagOptionsSerialization.serializedSize(field.options));
				BagKeySerialization.write(level, field, buf);
				finish();
			}

			void writeKeyedHeader(BagKey.Keyed field, int level) {
				ByteBuffer buf = emptyBag(
						field.key.remaining() + BagOptionsSerialization.serializedSize(field.options));
				BagKeySerialization.write(level, field, buf);
				finish();
			}

		}
	}

}
