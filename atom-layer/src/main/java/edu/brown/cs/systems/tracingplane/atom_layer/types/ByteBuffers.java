package edu.brown.cs.systems.tracingplane.atom_layer.types;

import java.nio.ByteBuffer;
import java.util.Comparator;

public class ByteBuffers {

	public static final Comparator<ByteBuffer> LEXICOGRAPHIC_COMPARATOR = UnsignedByteBuffer
			.lexicographicalComparator();

	/**
	 * Copies all remaining bytes from src to dest. This is done without
	 * modifying the src's position. However, the dest's position is updated.
	 */
	public static void copyTo(ByteBuffer src, ByteBuffer dest) {
		if (src == null || dest == null) {
			return;
		}
		if (src.hasArray()) {
			byte[] array = src.array();
			int offset = src.arrayOffset() + src.position();
			int length = src.remaining();
			dest.put(array, offset, length);
		} else {
			for (int i = src.position(); i < src.limit(); i++) {
				dest.put(src.get(i));
			}
		}
	}
	
	public static ByteBuffer copyRemaining(ByteBuffer src) {
		ByteBuffer buf = ByteBuffer.allocate(src.remaining());
		copyTo(src, buf);
		return buf;
	}
	
	public static byte[] copyRemainingBytes(ByteBuffer src) {
		return copyRemaining(src).array();
	}
	
	

}
