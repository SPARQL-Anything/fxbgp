package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.model.TriplifierHTTPException;

import java.io.IOException;
import java.util.Properties;

@Deprecated
public interface FXParser {
    void triplify(Properties properties, FXParserEventsHandler eventsHandler) throws IOException, TriplifierHTTPException;
}
