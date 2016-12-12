package edu.brown.cs.systems.tracingplane.atom_layer.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import edu.brown.cs.systems.tracingplane.atom_layer.protocol.AtomLayerOverflow.TrimExtent;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ProtobufVarint;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ProtobufVarint.EndOfStreamException;
import edu.brown.cs.systems.tracingplane.atom_layer.types.ProtobufVarint.MalformedVarintException;

/**
 * <p>
 * The underlying serialization format of atoms is to simply length-prefix the bytes of each atom. Length prefixes are
 * written using Protocol Buffers unsigned varints.</p>
 */
public class AtomLayerSerialization {

    static final Logger log = LoggerFactory.getLogger(AtomLayerSerialization.class);

    private AtomLayerSerialization() {}

    public static List<ByteBuffer> deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        } else {
            return deserialize(bytes, 0, bytes.length);
        }
    }

    public static List<ByteBuffer> deserialize(byte[] bytes, int offset, int length) {
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

    public static ByteBuffer readAtom(InputStream input) throws EndOfStreamException, MalformedVarintException, IOException {
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

    public static List<ByteBuffer> readFrom(InputStream input) throws IOException {
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

    public static int serializedSize(ByteBuffer atom) {
        return atom.remaining() + ProtobufVarint.sizeOf(atom.remaining());
    }

    public static int serializedSize(List<ByteBuffer> atoms) {
        int size = 0;
        for (ByteBuffer atom : atoms) {
            size += serializedSize(atom);
        }
        return size;
    }

    public static void writeAtom(ByteBuffer atom, ByteBuffer to) {
        ProtobufVarint.writeRawVarint32(to, atom.remaining());
        int position = atom.position();
        to.put(atom);
        atom.position(position);
    }
    
    public static void writeAtom(ByteBuffer atom, OutputStream out) throws IOException {
        ProtobufVarint.writeRawVarint32(out, atom.remaining());
        out.write(atom.array(), atom.arrayOffset() + atom.position(), atom.remaining());
    }

    public static byte[] serialize(List<ByteBuffer> atoms) {
        if (atoms == null || atoms.size() == 0) {
            return null;
        }
        ByteBuffer buf = ByteBuffer.allocate(serializedSize(atoms));
        for (ByteBuffer atom : atoms) {
            writeAtom(atom, buf);
        }
        return buf.array();
    }

    public static byte[] serialize(List<ByteBuffer> atoms, int limit) {
        if (atoms == null || atoms.size() == 0) {
            return null;
        }
        TrimExtent trim = AtomLayerOverflow.determineTrimExtent(atoms, limit);
        ByteBuffer buf = ByteBuffer.allocate(trim.serializedSize);
        for (int i = 0; i < trim.atomCount; i++) {
            writeAtom(atoms.get(i), buf);
        }
        if (trim.overflow) {
            writeAtom(AtomLayerOverflow.OVERFLOW_MARKER, buf);
        }
        return buf.array();
    }

    public static void write(OutputStream out, List<ByteBuffer> atoms) throws IOException {
        if (out == null || atoms == null || atoms.size() == 0) {
            return;
        }
        ProtobufVarint.writeRawVarint32(out, serializedSize(atoms));
        for (ByteBuffer atom : atoms) {
            writeAtom(atom, out);
        }
    }

    public static void write(OutputStream out, List<ByteBuffer> atoms, int limit) throws IOException {
        if (out == null || atoms == null || atoms.size() == 0) {
            return;
        }
        TrimExtent trim = AtomLayerOverflow.determineTrimExtent(atoms, limit);
        ProtobufVarint.writeRawVarint32(out, trim.serializedSize);
        for (int i = 0; i < trim.atomCount; i++) {
            writeAtom(atoms.get(i), out);
        }
        if (trim.overflow) {
            writeAtom(AtomLayerOverflow.OVERFLOW_MARKER, out);
        }
    }

}
