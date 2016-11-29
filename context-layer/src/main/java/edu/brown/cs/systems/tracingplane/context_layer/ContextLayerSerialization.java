package edu.brown.cs.systems.tracingplane.context_layer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brown.cs.systems.tracingplane.context_layer.types.ProtobufVarint;
import edu.brown.cs.systems.tracingplane.context_layer.types.ProtobufVarint.EndOfStreamException;
import edu.brown.cs.systems.tracingplane.context_layer.types.ProtobufVarint.MalformedVarintException;

public class ContextLayerSerialization {

	static final Logger log = LoggerFactory.getLogger(ContextLayerSerialization.class);

	private ContextLayerSerialization() {
	}

	static List<ByteBuffer> deserialize(byte[] bytes) {
		if (bytes == null) {
			return null;
		} else {
			return deserialize(bytes, 0, bytes.length);
		}
	}

	static List<ByteBuffer> deserialize(byte[] bytes, int offset, int length) {
		if (bytes == null || length <= 0) {
			return null;
		}
		List<ByteBuffer> bags = new ArrayList<>();

		ByteBuffer in = ByteBuffer.wrap(bytes, offset, length);
		while (in.remaining() > 0) {
			try {
				int bagSize = ProtobufVarint.readRawVarint32(in);
				bags.add(ByteBuffer.wrap(bytes, offset + in.position(), bagSize));
				in.position(in.position() + bagSize);
			} catch (MalformedVarintException e) {
				String msg = String.format("Malformed length prefix for bag %d (%d/%d bytes)", bags.size(),
						in.position(), length);
				log.warn(msg, e);
				break;
			} catch (BufferUnderflowException | IndexOutOfBoundsException | IllegalArgumentException e) {
				String msg = String.format("Premature end of baggage at bag %d (%d/%d bytes)", bags.size(),
						in.position(), length);
				log.warn(msg, e);
				break;
			}
		}

		if (bags.size() == 0) {
			return null;
		} else {
			return bags;
		}
	}

	static ByteBuffer readBag(InputStream input) throws EndOfStreamException, MalformedVarintException, IOException {
		int bagSize = ProtobufVarint.readRawVarint32(input);
		byte[] bagData = new byte[bagSize];
		int position = 0;
		while (position < bagSize) {
			int numRead = input.read(bagData, position, bagSize - position);
			if (numRead < 0) {
				log.warn("Premature end of bag after %d/%d bytes", position, bagSize);
				return null;
			} else {
				position += numRead;
			}
		}
		return ByteBuffer.wrap(bagData);
	}

	static List<ByteBuffer> readFrom(InputStream input) throws IOException {
		if (input == null) {
			return null;
		}

		final int length;
		try {
			length = ProtobufVarint.readRawVarint32(input);
		} catch (EndOfStreamException e) {
			log.warn("Reached end of stream before baggage could be read");
			return null;
		}

		final byte[] bytes = new byte[length];
		int position = 0, numRead;
		while (position < length) {
			numRead = input.read(bytes, position, length - position);
			if (numRead < 0) {
				log.warn("Reached end of stream after reading only %d/%d baggage bytes", position, length);
				byte[] truncated = new byte[position];
				System.arraycopy(bytes, 0, truncated, 0, position);
				return deserialize(truncated, 0, position);
			} else {
				position += numRead;
			}
		}

		return deserialize(bytes);
	}
	
	static int serializedSize(ByteBuffer bag) {
		return bag.remaining() + ProtobufVarint.sizeOf(bag.remaining());
	}
	
	static int serializedSize(List<ByteBuffer> bags) {
		int size = 0;
		for (ByteBuffer bag : bags) {
			size += serializedSize(bag);
		}
		return size;
	}
	
	private static final class TrimExtent {
		int bagCount = 0;
		int serializedSize = 0;
		boolean overflow = false;
		public TrimExtent(int bagCount, int serializedSize, boolean overflow) {
			this.bagCount = bagCount;
			this.serializedSize = serializedSize;
			this.overflow = overflow;
		}
	}
	
	static TrimExtent determineTrimExtent(List<ByteBuffer> bags, int limit) {
		if (limit <= 0) {
			return new TrimExtent(bags.size(), serializedSize(bags), false);
		}
		int[] serializationCutoffs = new int[bags.size()];
		int size = 0;
		for (int i = 0; i < bags.size(); i++) {
			size += serializedSize(bags.get(i));
			serializationCutoffs[i] = size;
		}
		if (size <= limit) {
			return new TrimExtent(bags.size(), size, false);
		}
		int overflowMarkerSize = serializedSize(ContextLayer.OVERFLOW_MARKER);
		for (int i = bags.size()-1; i > 0; i--) {
			size = serializationCutoffs[i-1] + overflowMarkerSize;
			if (size <= limit) {
				return new TrimExtent(i, size, true);
			}
		}
		return new TrimExtent(0, overflowMarkerSize, true);
	}
	
	static List<ByteBuffer> trimToSize(List<ByteBuffer> bags, int limit) {
		TrimExtent extent = determineTrimExtent(bags, limit);
		if (extent.overflow) {
			List<ByteBuffer> subList = new ArrayList<>(extent.bagCount+1);
			subList.addAll(bags.subList(0, extent.bagCount));
			subList.add(ContextLayer.OVERFLOW_MARKER);
			return subList;
		} else {
			return bags;
		}
	}
	
	static void writeBag(ByteBuffer bag, ByteBuffer to) {
		ProtobufVarint.writeRawVarint32(to, bag.remaining());
		int position = bag.position();
		to.put(bag);
		bag.position(position);
	}

	static byte[] serialize(List<ByteBuffer> bags) {
		if (bags == null || bags.size() == 0) {
			return null;
		}
		ByteBuffer buf = ByteBuffer.allocate(serializedSize(bags));
		for (ByteBuffer bag : bags) {
			writeBag(bag, buf);
		}
		return buf.array();
	}

	static byte[] serialize(List<ByteBuffer> bags, int limit) {
		if (bags == null || bags.size() == 0) {
			return null;
		}
		TrimExtent trim = determineTrimExtent(bags, limit);
		ByteBuffer buf = ByteBuffer.allocate(trim.serializedSize);
		for (int i = 0; i < trim.bagCount; i++) {
			writeBag(bags.get(i), buf);
		}
		if (trim.overflow) {
			writeBag(ContextLayer.OVERFLOW_MARKER, buf);
		}
		return buf.array();
	}

	static void write(OutputStream out, List<ByteBuffer> bags) throws IOException {
		if (out == null || bags == null || bags.size() == 0) {
			return;
		}
		ProtobufVarint.writeRawVarint32(out, serializedSize(bags));
		for (ByteBuffer bag : bags) {
			ProtobufVarint.writeRawVarint32(out, bag.remaining());
			out.write(bag.array(), bag.arrayOffset() + bag.position(), bag.remaining());
		}
	}

}
