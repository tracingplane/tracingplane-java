package edu.brown.cs.systems.tracingplane.baggage_layer;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

public interface BaggageLayer {
	
	public boolean contentsDidOverflow(BaggageContents bag);
	
	public boolean contentsWereTrimmed(BaggageContents bag);
	
	public boolean childrenDidOverflow(BaggageContents bag);
	
	public boolean childrenWereTrimmed(BaggageContents bag);
	
	public boolean hasData(BaggageContents bag);
	
	public List<ByteBuffer> getData(BaggageContents bag);
	
	public void addData(BaggageContents bag, List<ByteBuffer> additionalData);
	
	public void replaceData(BaggageContents bag, List<ByteBuffer> newData);
	
	public void removeData(BaggageContents bag);
	
	public boolean hasIndexedChild(BaggageContents bag, ByteBuffer child);
	
	public boolean hasKeyedChild(BaggageContents bag, ByteBuffer child);
	
	public BaggageContents getIndexedChild(BaggageContents bag, ByteBuffer child);
	
	public BaggageContents getKeyedChild(BaggageContents bag, ByteBuffer child);
	
	public BaggageContents addIndexedChild(BaggageContents bag, ByteBuffer child);
	
	public BaggageContents addKeyedChild(BaggageContents bag, ByteBuffer child);
	
	public void removeIndexedChild(BaggageContents bag, ByteBuffer child);
	
	public void removeKeyedChild(BaggageContents bag, ByteBuffer child);
	
	public Collection<ByteBuffer> indexedChildren(BaggageContents bag);
	
	public Collection<ByteBuffer> keyedChildren(BaggageContents bag);

}
