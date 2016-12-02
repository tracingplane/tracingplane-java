package edu.brown.cs.systems.tracingplane.baggage_layer.impl_old2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.brown.cs.systems.tracingplane.baggage_layer.impl_old3.BagParser;

public class Parsers {
	
	public static BagParser<Long> firstLong = new BagParser<Long>() {
		public Long parse(Iterator<ByteBuffer> data) {
			while (data.hasNext()) {
				ByteBuffer buf = data.next();
				if (buf.remaining() == 8) {
					Long value = buf.getLong();
					buf.position(buf.position()-8);
					return value;
				}
			}
			return null;
		}		
	};
	
	public static BagParser<List<Long>> allLongsAsList = new BagParser<List<Long>>() {
		public List<Long> parse(Iterator<ByteBuffer> data) {
			List<Long> out = null;
			while (data.hasNext()) {
				ByteBuffer buf = data.next();
				if (buf.remaining() == 8) {
					Long value = buf.getLong();
					buf.position(buf.position()-8);
					if (out == null) {
						out = new ArrayList<Long>();
					}
					out.add(value);
				}
			}
			return out;
		}
	};

}
