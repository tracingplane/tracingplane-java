package edu.brown.cs.systems.tracingplane.context_layer.types;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Duplicates logic of Protocol Buffers variable length integer encoding, but
 * with different logic for exceptions. This enables us to handle end-of-stream
 * differently to malformed length prefix exceptions or other IO exceptions
 */
public class ProtobufVarInt {

	public static final class EndOfStreamException extends InvalidProtocolBufferException {
		public EndOfStreamException() {
			super("While parsing a protocol message, the input ended unexpectedly "
					+ "in the middle of a field.  This could mean either than the "
					+ "input has been truncated or that an embedded message " + "misreported its own length.");
		}
	}

	public static final class MalformedVarintException extends InvalidProtocolBufferException {
		public MalformedVarintException(int size) {
			super("Encountered a malformed varint");
		}
	}

	private ProtobufVarInt() {
	}

	private static byte readByte(InputStream in) throws EndOfStreamException, IOException {
		int nextByte = in.read();
		if (nextByte == -1) {
			throw new EndOfStreamException();
		}
		return (byte) nextByte;
	}

	public static int readRawVarint32(InputStream in)
			throws EndOfStreamException, MalformedVarintException, IOException {
		byte tmp = readByte(in);
		if (tmp >= 0) {
			return tmp;
		}
		int result = tmp & 0x7f;
		if ((tmp = readByte(in)) >= 0) {
			result |= tmp << 7;
		} else {
			result |= (tmp & 0x7f) << 7;
			if ((tmp = readByte(in)) >= 0) {
				result |= tmp << 14;
			} else {
				result |= (tmp & 0x7f) << 14;
				if ((tmp = readByte(in)) >= 0) {
					result |= tmp << 21;
				} else {
					result |= (tmp & 0x7f) << 21;
					result |= (tmp = readByte(in)) << 28;
					if (tmp < 0) {
						// Discard upper 32 bits.
						for (int i = 0; i < 5; i++) {
							if (readByte(in) >= 0) {
								return result;
							}
						}
						throw new MalformedVarintException(10);
					}
				}
			}
		}
		return result;
	}

	public static void writeRawVarint32(ByteBuffer buf, int value) {
		while (true) {
			if ((value & ~0x7F) == 0) {
				buf.put((byte) value);
				return;
			} else {
				buf.put((byte) ((value & 0x7F) | 0x80));
				value >>>= 7;
			}
		}
	}
	
	public static void writeRawVarint32(OutputStream out, int value) throws IOException {
		while (true) {
			if ((value & ~0x7F) == 0) {
				out.write(value);
				return;
			} else {
				out.write((value & 0x7F) | 0x80);
				value >>>= 7;
			}
		}
	}

	public static int sizeOf(int value) {
		int numBytes = 0;
		do {
			value >>= 7;
			numBytes++;
		} while (value != 0);
		return numBytes;
	}

}
