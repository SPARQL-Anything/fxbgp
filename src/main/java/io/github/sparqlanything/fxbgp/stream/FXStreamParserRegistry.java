package io.github.sparqlanything.fxbgp.stream;

import java.util.Properties;

public class FXStreamParserRegistry {

    static FXStreamParser get(Properties properties) {
        String mediaType = (String)properties.get("media-type");
        if(mediaType.contains("json")){
            return new JSONStreamParser(properties);
        }else if(mediaType.contains("csv")){
            return new CSVStreamParser(properties);
        }else if(mediaType.contains("xml")){
            return new XMLStreamParser(properties);
        }
        throw new RuntimeException("media-type not found");
    }
}
