package edu.brown.cs.systems.tracingplane.context_layer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import edu.brown.cs.systems.tracingplane.context_layer.ContextLayerListener.ContextLayerListenerContainer;
import edu.brown.cs.systems.tracingplane.transit_layer.TransitLayer;

public class ContextLayerConfig {

	private static final Logger log = LoggerFactory.getLogger(TransitLayer.class);

	private static final String CONTEXT_LAYER_LISTENERS_KEY = "tracingplane.context-layer.listeners";

	public List<String> contextLayerListenerClasses;

	public ContextLayerConfig() {
		Config conf = ConfigFactory.load();
		contextLayerListenerClasses = conf.getStringList(CONTEXT_LAYER_LISTENERS_KEY);
		for (String className : contextLayerListenerClasses) {
			try {
				Class.forName(className);
			} catch (ClassNotFoundException e) {
				log.error("The configured context layer listener class {}=\"{}\" was not found");
			}
		}
	}

	/**
	 * Creates and returns all context layer listeners specified by the
	 * configuration key "tracingplane.context-layer.listeners"
	 * 
	 * If any exceptions are thrown while trying to instantiate a listener, the
	 * listener will be skipped and the exception logged.
	 * 
	 * This method returns a ContextLayerListenerContainer containing the
	 * instantiated listeners
	 */
	public ContextLayerListener tryCreateListeners() {
		List<ContextLayerListener> listeners = Lists.newArrayList();
		for (String className : contextLayerListenerClasses) {
			try {
				listeners.add((ContextLayerListener) Class.forName(className).newInstance());
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				log.error(String.format("Unable to create configured default context layer listener %s (config key %s)",
						className, CONTEXT_LAYER_LISTENERS_KEY), e);
			}
		}
		ContextLayerListener[] listenerArray = new ContextLayerListener[listeners.size()];
		return new ContextLayerListenerContainer(listeners.toArray(listenerArray));
	}

}
