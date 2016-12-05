package edu.brown.cs.systems.tracingplane.context_layer.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brown.cs.systems.tracingplane.context_layer.ContextLayer;
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
		List<ByteBuffer> atoms = new ArrayList<>();

		ByteBuffer in = ByteBuffer.wrap(bytes, offset, length);
		while (in.remaining() > 0) {
			try {
				int atomSize = ProtobufVarint.readRawVarint32(in);
				atoms.add(ByteBuffer.wrap(bytes, offset + in.position(), atomSize));
				in.position(in.position() + atomSize);
			} catch (MalformedVarintException e) {
				String msg = String.format("Malformed length prefix for atom %d (%d/%d bytes)", atoms.size(),
						in.position(), length);
				log.warn(msg, e);
				break;
			} catch (BufferUnderflowException | IndexOutOfBoundsException | IllegalArgumentException e) {
				String msg = String.format("Premature end of baggage at atom %d (%d/%d bytes)", atoms.size(),
						in.position(), length);
				log.warn(msg, e);
				break;
			}
		}

		if (atoms.size() == 0) {
			return null;
		} else {
			return atoms;
		}
	}

	static ByteBuffer readAtom(InputStream input) throws EndOfStreamException, MalformedVarintException, IOException {
		int atomSize = ProtobufVarint.readRawVarint32(input);
		byte[] atomData = new byte[atomSize];
		int position = 0;
		while (position < atomSize) {
			int numRead = input.read(atomData, position, atomSize - position);
			if (numRead < 0) {
				log.warn("Premature end of atom after %d/%d bytes", position, atomSize);
				return null;
			} else {
				position += numRead;
			}
		}
		return ByteBuffer.wrap(atomData);
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
	
	static int serializedSize(ByteBuffer atom) {
		return atom.remaining() + ProtobufVarint.sizeOf(atom.remaining());
	}
	
	static int serializedSize(List<ByteBuffer> atoms) {
		int size = 0;
		for (ByteBuffer atom : atoms) {
			size += serializedSize(atom);
		}
		return size;
	}
	
	private static final class TrimExtent {
		int atomCount = 0;
		int serializedSize = 0;
		boolean overflow = false;
		public TrimExtent(int atomCount, int serializedSize, boolean overflow) {
			this.atomCount = atomCount;
			this.serializedSize = serializedSize;
			this.overflow = overflow;
		}
	}
	
	static TrimExtent determineTrimExtent(List<ByteBuffer> atoms, int limit) {
		if (limit <= 0) {
			return new TrimExtent(atoms.size(), serializedSize(atoms), false);
		}
		int[] serializationCutoffs = new int[atoms.size()];
		int size = 0;
		for (int i = 0; i < atoms.size(); i++) {
			size += serializedSize(atoms.get(i));
			serializationCutoffs[i] = size;
		}
		if (size <= limit) {
			return new TrimExtent(atoms.size(), size, false);
		}
		int overflowMarkerSize = serializedSize(ContextLayer.OVERFLOW_MARKER);
		for (int i = atoms.size()-1; i > 0; i--) {
			size = serializationCutoffs[i-1] + overflowMarkerSize;
			if (size <= limit) {
				return new TrimExtent(i, size, true);
			}
		}
		return new TrimExtent(0, overflowMarkerSize, true);
	}
	
	static List<ByteBuffer> trimToSize(List<ByteBuffer> atoms, int limit) {
		TrimExtent extent = determineTrimExtent(atoms, limit);
		if (extent.overflow) {
			List<ByteBuffer> subList = new ArrayList<>(extent.atomCount+1);
			subList.addAll(atoms.subList(0, extent.atomCount));
			subList.add(ContextLayer.OVERFLOW_MARKER);
			return subList;
		} else {
			return atoms;
		}
	}
	
	static void writeAtom(ByteBuffer atom, ByteBuffer to) {
		ProtobufVarint.writeRawVarint32(to, atom.remaining());
		int position = atom.position();
		to.put(atom);
		atom.position(position);
	}

	static byte[] serialize(List<ByteBuffer> atoms) {
		if (atoms == null || atoms.size() == 0) {
			return null;
		}
		ByteBuffer buf = ByteBuffer.allocate(serializedSize(atoms));
		for (ByteBuffer atom : atoms) {
			writeAtom(atom, buf);
		}
		return buf.array();
	}

	static byte[] serialize(List<ByteBuffer> atoms, int limit) {
		if (atoms == null || atoms.size() == 0) {
			return null;
		}
		TrimExtent trim = determineTrimExtent(atoms, limit);
		ByteBuffer buf = ByteBuffer.allocate(trim.serializedSize);
		for (int i = 0; i < trim.atomCount; i++) {
			writeAtom(atoms.get(i), buf);
		}
		if (trim.overflow) {
			writeAtom(ContextLayer.OVERFLOW_MARKER, buf);
		}
		return buf.array();
	}

	static void write(OutputStream out, List<ByteBuffer> atoms) throws IOException {
		if (out == null || atoms == null || atoms.size() == 0) {
			return;
		}
		ProtobufVarint.writeRawVarint32(out, serializedSize(atoms));
		for (ByteBuffer atom : atoms) {
			ProtobufVarint.writeRawVarint32(out, atom.remaining());
			out.write(atom.array(), atom.arrayOffset() + atom.position(), atom.remaining());
		}
	}

}
