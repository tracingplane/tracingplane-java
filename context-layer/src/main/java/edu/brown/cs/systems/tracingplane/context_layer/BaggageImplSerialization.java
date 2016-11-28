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

public class BaggageImplSerialization {

	static final Logger log = LoggerFactory.getLogger(BaggageImplSerialization.class);

	private BaggageImplSerialization() {
	}

	static BaggageImpl deserialize(byte[] bytes) {
		if (bytes == null) {
			return null;
		} else {
			return deserialize(bytes, 0, bytes.length);
		}
	}

	static BaggageImpl deserialize(byte[] bytes, int offset, int length) {
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
			return new BaggageImpl(bags);
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

	static BaggageImpl readFrom(InputStream input) throws IOException {
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

	static byte[] serialize(BaggageImpl baggage) {
		if (baggage == null || baggage.contents.bags.size() == 0) {
			return null;
		}
		ByteBuffer buf = ByteBuffer.allocate(baggage.serializedSize());
		for (ByteBuffer bag : baggage.contents.bags) {
			ProtobufVarint.writeRawVarint32(buf, bag.remaining());
			int position = bag.position();
			buf.put(bag);
			bag.position(position);
		}
		return buf.array();
	}

	static void write(OutputStream out, BaggageImpl baggage) throws IOException {
		if (baggage == null || out == null || baggage.contents.bags.size() == 0) {
			return;
		}
		ProtobufVarint.writeRawVarint32(out, baggage.serializedSize());
		for (ByteBuffer bag : baggage.contents.bags) {
			ProtobufVarint.writeRawVarint32(out, bag.remaining());
			out.write(bag.array(), bag.arrayOffset() + bag.position(), bag.remaining());
		}
	}

}
