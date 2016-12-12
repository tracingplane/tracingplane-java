package edu.brown.cs.systems.tracingplane.atom_layer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import edu.brown.cs.systems.tracingplane.atom_layer.impl.RawAtomLayerFactory;

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
            log.error("The configured atom layer class {}=\"{}\" was not found; defaulting to raw atom layer",
                      ATOM_LAYER_IMPLEMENTATION_KEY, atomLayerFactory);
        }
    }

    public AtomLayer<?> createAtomLayer() throws InstantiationException, IllegalAccessException,
                                          ClassNotFoundException {
        return ((AtomLayerFactory) Class.forName(atomLayerFactory).newInstance()).newAtomLayer();
    }
    
    private static AtomLayer<?> defaultAtomLayer = null;

    public static synchronized AtomLayer<?> defaultAtomLayer() {
        if (defaultAtomLayer == null) {
            AtomLayerConfig config = new AtomLayerConfig();
            try {
                defaultAtomLayer =  config.createAtomLayer();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                log.error(String.format("Unable to instantiate default atom layer factory %s, defaulting to %s",
                                        config.atomLayerFactory, RawAtomLayerFactory.class.getName()));
                defaultAtomLayer = new RawAtomLayerFactory().newAtomLayer();
            }
        }
        return defaultAtomLayer;
    }

}
