package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.model.TriplifierHTTPException;

import java.io.IOException;
import java.util.Properties;

public interface FXParser {
    void triplify(Properties properties, TriplifierEventsHandler eventsHandler) throws IOException, TriplifierHTTPException;
}
