package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.BaggageWriter;
import edu.brown.cs.systems.tracingplane.baggage_layer.protocol.Serializer;

public class GenericBaggageContentsSerializer implements Serializer<GenericBaggageContents> {

	@Override
	public void serialize(BaggageWriter builder, GenericBaggageContents instance) {
		if (instance != null) {
			builder.didTrimHere(instance.dataWasTrimmed);

			if (instance.data != null) {
				for (ByteBuffer data : instance.data) {
					builder.writeBytes(data);
				}
			}

			if (instance.children != null) {
				List<BagKey> childKeys = Lists.newArrayList(instance.children.keySet());
				Collections.sort(childKeys);
				for (BagKey key : childKeys) {
					builder.enter(key);
					serialize(builder, instance.children.get(key));
					builder.exit();
				}
			}
		}
	}

}
