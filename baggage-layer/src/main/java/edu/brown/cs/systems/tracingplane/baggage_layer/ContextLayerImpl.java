package edu.brown.cs.systems.tracingplane.baggage_layer;

import java.nio.ByteBuffer;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.brown.cs.systems.tracingplane.context_layer.ContextBaggage;
import edu.brown.cs.systems.tracingplane.context_layer.ContextLayer;

public class ContextLayerImpl implements ContextLayer {

	private static final Logger log = LoggerFactory.getLogger(ContextLayerImpl.class);

	final BaggageLayerConfig config;
	
	public final BaggageLayer baggageLayer;
	
	public ContextLayerImpl() {
		this(new BaggageLayerConfig());
	}
	
	public ContextLayerImpl(BaggageLayerConfig config) {
		this.config = config;
		this.baggageLayer = config.createBaggageLayerInstanceOrDefault();
	}


	@Override
	public void discard(ContextBaggage baggage) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ContextBaggage branch(ContextBaggage from) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContextBaggage join(ContextBaggage left, ContextBaggage right) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContextBaggage wrap(List<ByteBuffer> bags) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ByteBuffer> atoms(ContextBaggage baggage) {
		// TODO Auto-generated method stub
		return null;
	}

}
