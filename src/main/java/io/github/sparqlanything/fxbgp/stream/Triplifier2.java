package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.model.FacadeXGraphBuilder;
import io.github.sparqlanything.model.Triplifier;
import io.github.sparqlanything.model.TriplifierHTTPException;

import java.io.IOException;
import java.util.Properties;

@Deprecated
public interface Triplifier2 extends Triplifier {
    // XXX Ignore old method
    default void triplify(Properties properties, FacadeXGraphBuilder builder) throws IOException, TriplifierHTTPException {}

    static FXParser get(Properties properties) {
        String mediaType = (String)properties.get("media-type");
        if(mediaType.contains("json")){
            return new JSONTriplifier2();
        }else if(mediaType.contains("csv")){
            return new CSVTriplifier2();
        }
        throw new RuntimeException("media-type not found");
    }

}
