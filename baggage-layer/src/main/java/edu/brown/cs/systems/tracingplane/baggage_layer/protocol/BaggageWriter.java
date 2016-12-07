package edu.brown.cs.systems.tracingplane.baggage_layer.protocol;

import java.nio.ByteBuffer;
import java.util.List;

import com.google.common.collect.Lists;

import edu.brown.cs.systems.tracingplane.atom_layer.BaggageAtoms;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ByteBuffers;
import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageContents;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.AtomPrefixes.DataAtom;

/**
 * Used for writing out baggage atoms that adhere to the baggage protocol
 */
public class BaggageWriter {

	public final List<ByteBuffer> atoms = Lists.newArrayList();

	private final SharedBackingBuffer backing = new SharedBackingBuffer(1024);

	public void writeHeader(int level, BagKey field) {
		if (field instanceof BagKey.Indexed) {
			ByteBuffer buf = backing.newAtom(10 + BagOptionsSerialization.serializedSize(field.options));
			BagKeySerialization.write(level, (BagKey.Indexed) field, buf);
		} else if (field instanceof BagKey.Keyed) {
			ByteBuffer buf = backing.newAtom(
					((BagKey.Keyed) field).key.remaining() + BagOptionsSerialization.serializedSize(field.options));
			BagKeySerialization.write(level, (BagKey.Indexed) field, buf);
		}
	}
	
	public void writeOverflowMarker() {
		atoms.add(BaggageAtoms.OVERFLOW_MARKER);
	}
	
	public void writeTrimMarker() {
		atoms.add(BaggageContents.TRIM_MARKER);
	}

	/**
	 * Creates a byte buffer with {@code expectedSize} bytes of free space. The
	 * atom can be written to as normal.  Not all of the free space must be filled
	 */
	public ByteBuffer newDataAtom(int expectedSize) {
		ByteBuffer buf = backing.newAtom(expectedSize + 1);
		buf.put(DataAtom.prefix);
		return buf;
	}
	
	/**
	 * Write a data atom with the provided buf content
	 */
	public void writeBytes(ByteBuffer buf) {
		// See if this is already prefixed
		if (buf.position() > 0 && buf.get(buf.position() - 1) == DataAtom.prefix) {
			buf.position(buf.position() - 1);
			atoms.add(buf);
		} else {
			atoms.add(ByteBuffers.copyWithPrefix(DataAtom.prefix, buf));
		}
	}
	
	/**
	 * Write a data atom with an integer value
	 */
	public void writeInt(int value) {
		newDataAtom(Integer.BYTES).putInt(value);
	}
	
	/**
	 * Write a data atom with a long value
	 */
	public void writeLong(long value) {
		newDataAtom(Long.BYTES).putLong(value);
	}
	
	/**
	 * Ensure that any buffers created with newDataAtom are finished.
	 */
	public void flush() {
		backing.finish();
	}

	private class SharedBackingBuffer {
		final int backingBufferSize;

		ByteBuffer current;
		ByteBuffer backingBuffer;

		SharedBackingBuffer(int backingBufferSize) {
			this.backingBufferSize = backingBufferSize;
		}

		void ensureCapacity(int requiredCapacity) {
			if (backingBuffer == null || backingBuffer.remaining() < requiredCapacity) {
				backingBuffer = ByteBuffer.allocate(Math.max(backingBufferSize, requiredCapacity));
			}
		}

		ByteBuffer newAtom(int expectedSize) {
			finish();
			ensureCapacity(expectedSize);
			current = backingBuffer.duplicate();
			return current;
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

	}

}
