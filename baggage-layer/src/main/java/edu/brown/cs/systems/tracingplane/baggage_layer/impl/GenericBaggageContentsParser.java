package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.collect.Iterators;

import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.Parser;

public class GenericBaggageContentsParser extends Parser<GenericBaggageContents> {

	@Override
	protected GenericBaggageContents parseData(Iterator<ByteBuffer> data) {
		if (data.hasNext()) {
			GenericBaggageContents contents = new GenericBaggageContents();
			contents.data = new ArrayList<>();
			Iterators.addAll(contents.data, data);
			return contents;
		} else {
			return null;
		}
	}

	@Override
	protected void dataOverflow(GenericBaggageContents baggage) {
		baggage.dataDidOverflow = true;
	}

	@Override
	protected void childOverflow(GenericBaggageContents baggage) {
		baggage.childDidOverflow = true;
	}

	@Override
	protected Parser<?> getParserForChild(int childIndex, ByteBuffer childOptions) {
		return this;
	}

	@Override
	protected Parser<?> getParserForChild(ByteBuffer childKey, ByteBuffer childOptions) {
		return this;
	}

	@Override
	protected <C> GenericBaggageContents setChild(GenericBaggageContents baggage, int childIndex,
			ByteBuffer childOptions, C childData) {
		return null;
	}

	@Override
	protected <C> GenericBaggageContents setChild(GenericBaggageContents baggage, ByteBuffer childKey,
			ByteBuffer childOptions, C childData) {
		// TODO Auto-generated method stub
		return null;
	}

}
