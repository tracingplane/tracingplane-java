package edu.brown.cs.systems.tracingplane.context_layer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import edu.brown.cs.systems.tracingplane.context_layer.impl.BlindContextLayer;
import edu.brown.cs.systems.tracingplane.context_layer.listener.ContextLayerListener;
import edu.brown.cs.systems.tracingplane.context_layer.listener.ContextLayerListenerContainer;
import edu.brown.cs.systems.tracingplane.context_layer.listener.ContextLayerNullListener;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;

public class ContextLayerConfig {

	private static final Logger log = LoggerFactory.getLogger(TransitLayer.class);

	private static final String CONTEXT_LAYER_IMPLEMENTATION_KEY = "tracingplane.context-layer.implementation";
	private static final String CONTEXT_LAYER_LISTENERS_KEY = "tracingplane.context-layer.listeners";

	public String contextLayerImplementationClassName;
	public List<String> contextLayerListenerClassNames;

	public ContextLayerConfig() {
		Config conf = ConfigFactory.load();

		contextLayerImplementationClassName = conf.getString(CONTEXT_LAYER_IMPLEMENTATION_KEY);
		try {
			Class.forName(contextLayerImplementationClassName);
		} catch (ClassNotFoundException e) {
			log.error("The configured context layer class {}=\"{}\" was not found; defaulting to simple context layer");
		}

		contextLayerListenerClassNames = conf.getStringList(CONTEXT_LAYER_LISTENERS_KEY);
		for (String className : contextLayerListenerClassNames) {
			try {
				Class.forName(className);
			} catch (ClassNotFoundException e) {
				log.error("The configured context layer listener {} was not found (configuration key {})", className,
						CONTEXT_LAYER_LISTENERS_KEY);
			}
		}
	}

	public ContextLayer createContextLayerInstance()
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return (ContextLayer) Class.forName(contextLayerImplementationClassName).newInstance();
	}

	public ContextLayer createContextLayerInstanceOrDefault() {
		try {
			return createContextLayerInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			log.error(String.format(
					"Unable to create instance of context layer class %s=\"%s\", defaulting to BlindContextLayer",
					CONTEXT_LAYER_IMPLEMENTATION_KEY, contextLayerImplementationClassName), e);
			return new BlindContextLayer();
		}
	}

	public ContextLayerListener createContextLayerListener(String listenerClassName)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return (ContextLayerListener) Class.forName(listenerClassName).newInstance();
	}

	public ContextLayerListener createContextLayerListeners() {
		List<ContextLayerListener> listeners = new ArrayList<>();
		for (String listenerClassName : contextLayerListenerClassNames) {
			try {
				listeners.add(createContextLayerListener(listenerClassName));
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				log.error(String.format("Unable to create instance of context layer listener {} (configuration key {})",
						listenerClassName, CONTEXT_LAYER_LISTENERS_KEY), e);
			}
		}
		if (listeners.size() == 0) {
			return new ContextLayerNullListener();
		} else {
			return new ContextLayerListenerContainer(listeners);
		}
	}

}
