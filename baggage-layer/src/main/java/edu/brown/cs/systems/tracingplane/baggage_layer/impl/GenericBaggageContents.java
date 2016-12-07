package edu.brown.cs.systems.tracingplane.baggage_layer.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.brown.cs.systems.tracingplane.baggage_layer.BagKey;
import edu.brown.cs.systems.tracingplane.baggage_layer.BaggageContents;

public class GenericBaggageContents implements BaggageContents {

	public boolean dataDidOverflow = false;
	public boolean dataWasTrimmed = false;
	public boolean childDidOverflow = false;
	public boolean childWasTrimmed = false;
	public List<ByteBuffer> data = null;
	public Map<BagKey, GenericBaggageContents> children = null;

	public GenericBaggageContents branch() {
		// TODO Auto-generated method stub
		return null;
	}

	public GenericBaggageContents mergeWith(GenericBaggageContents other) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addData(ByteBuffer dataItem) {
		if (dataItem != null) {
			if (data == null) {
				data = new ArrayList<>();
			}
			data.add(dataItem);
		}
	}

	public void addChild(BagKey key, GenericBaggageContents child) {
		if (child != null) {
			if (children == null) {
				children = new HashMap<>();
			}
			children.put(key, child);
			childWasTrimmed |= child.dataWasTrimmed || child.childWasTrimmed;
		}
	}

}
