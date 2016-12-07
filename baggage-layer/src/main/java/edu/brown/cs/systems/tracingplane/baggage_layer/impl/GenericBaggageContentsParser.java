package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import java.nio.ByteBuffer;

import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageContents;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageReader;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.Parser;

// TODO: description and comments
public class GenericBaggageContentsParser implements Parser<GenericBaggageContents> {

	@Override
	public GenericBaggageContents parse(BaggageReader reader) {
		if (reader.hasNext()) {
			GenericBaggageContents baggage = new GenericBaggageContents();
			parseData(reader, baggage);
			parseChildren(reader, baggage);
			return baggage;
		}
		return null;
	}

	private void parseData(BaggageReader reader, GenericBaggageContents baggage) {
		ByteBuffer nextData = null;
		while ((nextData = reader.nextData()) != null) {
			if (BaggageContents.TRIMMARKER_CONTENTS.equals(nextData)) {
				baggage.dataWasTrimmed = true;
			} else {
				baggage.addData(nextData);
			}
		}
		baggage.dataDidOverflow = reader.didOverflow();
	}

	private void parseChildren(BaggageReader reader, GenericBaggageContents baggage) {
		BagKey nextChild = null;
		while ((nextChild = reader.enter()) != null) {
			baggage.addChild(nextChild, parse(reader));
			reader.exit();
		}
		baggage.childDidOverflow = reader.didOverflow();
	}

}
