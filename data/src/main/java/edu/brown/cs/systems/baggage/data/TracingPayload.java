package edu.brown.cs.systems.baggage.data;

import java.nio.ByteBuffer;
import java.util.List;

public class TracingPayload {
	
	public final List<ByteBuffer> frames;
	
	public TracingPayload(List<ByteBuffer> frames) {
		this.frames = frames;
	}
//	
//	public static TracingPayload parseDelimitedFrom(ByteBuffer bytes) {
//		
//	}

}
