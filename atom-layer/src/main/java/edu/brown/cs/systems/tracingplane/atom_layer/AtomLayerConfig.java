package edu.brown.cs.systems.tracingplane.atom_layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class AtomLayerConfig {

	private static final Logger log = LoggerFactory.getLogger(AtomLayerConfig.class);

	private static final String ATOM_LAYER_IMPLEMENTATION_KEY = "tracingplane.atom-layer.factory";

	public String atomLayerFactory;

	public AtomLayerConfig() {
		Config conf = ConfigFactory.load();

		atomLayerFactory = conf.getString(ATOM_LAYER_IMPLEMENTATION_KEY);
		try {
			Class.forName(atomLayerFactory);
		} catch (ClassNotFoundException e) {
			log.error("The configured atom layer class {}=\"{}\" was not found; defaulting to raw atom layer");
		}
	}

}
