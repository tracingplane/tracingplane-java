package edu.brown.cs.systems.tracingplane.baggage_layer.prototype.baggage_buffers.xtrace;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brown.cs.systems.tracingplane.baggage_layer.impl.Parser2;

public class XTraceParser2 extends Parser2<XTrace> {

	static final Logger log = LoggerFactory.getLogger(XTraceParser2.class);

	@Override
	public XTrace parseData(Iterator<ByteBuffer> data) {
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
	public void dataOverflow(XTrace instance) {
		instance.overflow = true;
	}

	@Override
	public void childOverflow(XTrace instance) {
		instance.overflow = true;
	}

	@Override
	public Parser2<?> getParserForChild(int childIndex, ByteBuffer childOptions) {
		switch(childIndex) {
//		case 0: return taskIdParser;
//		case 1: return parentEventIdsParser;
		default: return null;
		}
	}

	@Override
	public Parser2<?> getParserForChild(ByteBuffer childKey, ByteBuffer childOptions) {
		return null;
	}

	@Override
	public <C> XTrace setChild(XTrace instance, int childIndex, ByteBuffer childOptions, C childData) {
		switch(childIndex) {
		case 0: instance.taskId = (Long) childData;
		case 1: instance.parentEventIds = (List<Long>) childData;
		default:
		}
		return instance;
	}

	@Override
	public <C> XTrace setChild(XTrace instance, ByteBuffer childKey, ByteBuffer childOptions, C childData) {
		return null;
	}
	

}
