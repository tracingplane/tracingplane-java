package edu.brown.cs.systems.tracingplane.baggage_layer.prototype.baggage_buffers.xtrace;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brown.cs.systems.tracingplane.baggage_layer.prototype.baggage_buffers.library.Field;
import edu.brown.cs.systems.tracingplane.baggage_layer.prototype.baggage_buffers.library.Parsers;
import edu.brown.cs.systems.tracingplane.baggage_layer.prototype2.BagParser;
import edu.brown.cs.systems.tracingplane.baggage_layer.prototype2.BagSerialization;
import edu.brown.cs.systems.tracingplane.baggage_layer.prototype2.BagSerializer;

public class XTraceSerialization implements BagSerialization<XTrace> {

	static final Logger log = LoggerFactory.getLogger(XTraceSerialization.class);
	
	public BagParser<XTrace> parser() {
		return parser;
	}
	
	public BagSerializer<XTrace> serializer() {
		return serializer;
	}
	
	public static interface BagWithXTraceField {
		public XTrace getXTrace();
	}
	
	static final BagParser<XTrace> parser = new BagParser<XTrace>() {
	
		@Override
		public XTrace parse(Iterator<ByteBuffer> data) {
			// Do not expect any data
			int numElements = 0;
			while (data.hasNext()) {
				data.next();
			}
			if (numElements > 0) {
				log.warn("XTraceMetadata should have no base data but found {} elements", numElements);
			}
			return new XTrace();
		}
		
		@Override
		public BagParser<?> getParserForChild(int childIndex, ByteBuffer childOptions) {
			if (childIndex >= 0 && childIndex < fields.length) {
				return fields[childIndex].parser();
			} else {
				return null;
			}
		}
	
		@Override
		public void setChild(int childIndex, ByteBuffer childOptions, XTrace parent, Object child) {
			fields[childIndex].setCast(parent, child);
		}
		
	};
	
	static final BagSerializer<XTrace> serializer = new BagSerializer<XTrace>() {

		@Override
		public void serialize(BagBuilder builder, XTrace xtrace) {
			if (xtrace != null && xtrace.overflow) {
				builder.markOverflow();
			}
		}

		@Override
		public void serializeChildren(ChildBuilder builder, XTrace xtrace) {
			for (int i = 0; i < fields.length; i++) {
				fields[i].serializeTo(builder, i, null, xtrace);
			}
		}
		
	};


	static interface XTraceField<T> extends Field<XTrace, T> {
	}

	static final XTraceField<Long> taskIdField = new XTraceField<Long>() {

		@Override
		public BagParser<Long> parser() {
			return Parsers.firstLong;
		}

		@Override
		public BagSerializer<Long> serializer() {
			// TODO: return long serializer
			return null;
		}

		@Override
		public void set(XTrace xtrace, Long taskId) {
			if (xtrace != null) {
				xtrace.taskId = taskId;
			}
		}

		@Override
		public Long getFrom(XTrace xtrace) {
			return xtrace == null ? null : xtrace.taskId;
		}

	};
	
	static final XTraceField<List<Long>> parentEventIdsField = new XTraceField<List<Long>>() {

		@Override
		public BagParser<List<Long>> parser() {
			return Parsers.allLongsAsList;
		}

		@Override
		public BagSerializer<List<Long>> serializer() {
			// TODO:
			return null;
		}

		@Override
		public void set(XTrace xtrace, List<Long> parentEventIds) {
			if (xtrace != null) {
				xtrace.parentEventIds = parentEventIds;
			}
		}

		@Override
		public List<Long> getFrom(XTrace xtrace) {
			if (xtrace != null && xtrace.parentEventIds != null && xtrace.parentEventIds.size() > 0) {
				return xtrace.parentEventIds;
			} else {
				return null;
			}
		}
		
	};

	static final XTraceField<?>[] fields = { 
		taskIdField,
		parentEventIdsField,
	};

}
