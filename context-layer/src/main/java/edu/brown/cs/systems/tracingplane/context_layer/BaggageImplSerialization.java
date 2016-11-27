package edu.brown.cs.systems.tracingplane.context_layer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.CodedInputStream;

import edu.brown.cs.systems.tracingplane.context_layer.types.ProtobufVarInt;
import edu.brown.cs.systems.tracingplane.context_layer.types.ProtobufVarInt.EndOfStreamException;
import edu.brown.cs.systems.tracingplane.context_layer.types.ProtobufVarInt.MalformedVarintException;

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
		CodedInputStream in = CodedInputStream.newInstance(bytes, offset, length);

		while (in.getBytesUntilLimit() > 0) {
			try {
				int bagLength = in.readRawVarint32();
				ByteBuffer bag = ByteBuffer.wrap(bytes, offset + in.getTotalBytesRead(), bagLength);
				in.skipRawBytes(bagLength);
				bags.add(bag);
			} catch (IndexOutOfBoundsException | IOException e) {
				String msg = String.format("Premature end of baggage at bag %d (%d/%d bytes)", bags.size(),
						in.getTotalBytesRead(), length);
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
		int bagSize = ProtobufVarInt.readRawVarint32(input);
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
			length = ProtobufVarInt.readRawVarint32(input);
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
		if (baggage == null || baggage.bags.size() == 0) {
			return null;
		}
		ByteBuffer buf = ByteBuffer.allocate(baggage.serializedSize());
		for (ByteBuffer bag : baggage.bags) {
			ProtobufVarInt.writeRawVarint32(buf, bag.remaining());
			int position = bag.position();
			buf.put(bag);
			bag.position(position);
		}
		return buf.array();
	}

	static void write(OutputStream out, BaggageImpl baggage) throws IOException {
		if (baggage == null || out == null || baggage.bags.size() == 0) {
			return;
		}
		ProtobufVarInt.writeRawVarint32(out, baggage.serializedSize());
		for (ByteBuffer bag : baggage.bags) {
			ProtobufVarInt.writeRawVarint32(out, bag.remaining());
			out.write(bag.array(), bag.arrayOffset() + bag.position(), bag.remaining());
		}
	}

}
