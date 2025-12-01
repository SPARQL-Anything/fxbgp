package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.model.FacadeXGraphBuilder;
import io.github.sparqlanything.model.Triplifier;
import io.github.sparqlanything.model.TriplifierHTTPException;

import java.io.IOException;
import java.util.Properties;

public interface Triplifier2 extends Triplifier {
    // XXX Ignore old method
    default void triplify(Properties properties, FacadeXGraphBuilder builder) throws IOException, TriplifierHTTPException {}
    default void triplify(Properties properties, TriplifierEventsHandler eventsHandler) throws IOException, TriplifierHTTPException {}

}
