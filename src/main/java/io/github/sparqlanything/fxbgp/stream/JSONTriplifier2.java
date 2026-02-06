package io.github.sparqlanything.fxbgp.stream;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.common.collect.Sets;
import io.github.sparqlanything.json.JSONTriplifier;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.PropertyUtils;
import io.github.sparqlanything.model.SPARQLAnythingConstants;
import io.github.sparqlanything.model.Triplifier;
import io.github.sparqlanything.model.TriplifierHTTPException;
import com.fasterxml.jackson.core.JsonFactory;
import io.github.sparqlanything.model.annotations.Example;
import io.github.sparqlanything.model.annotations.Option;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;

@Deprecated
public class JSONTriplifier2 implements FXParser {

    @Example(resource = "https://sparql-anything.cc/example1.json", description = "Retrieving the lists of stars of the TV Series named \"Friends\" and \"Cougar Town\".", query = " PREFIX xyz: <http://sparql.xyz/facade-x/data/> PREFIX fx: <http://sparql.xyz/facade-x/ns/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> CONSTRUCT { ?s ?p ?o . } WHERE { SERVICE <x-sparql-anything:location=https://sparql-anything.cc/example1.json> { fx:properties fx:json.path.1 \"$[?(@.name==\\\"Friends\\\")].stars\" ; fx:json.path.2 \"$[?(@.name==\\\"Cougar Town\\\")].stars\" . ?s ?p ?o } } ")
    @Example(resource = "https://sparql-anything.cc/example1.json", description = "Retrieving the language of the TV series named \"Friends\".", query = "PREFIX xyz: <http://sparql.xyz/facade-x/data/> PREFIX fx: <http://sparql.xyz/facade-x/ns/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT ?language WHERE { SERVICE <x-sparql-anything:location=https://sparql-anything.cc/example1.json> { fx:properties fx:json.path \"$[?(@.name==\\\"Friends\\\")]\" . _:b0 xyz:language ?language } }")
    @Example(resource = "https://sparql-anything.cc/example1.json", description = "Constructing a Facade-X RDF Graph selecting only containers that match the Json Path '$[?(@.name==\"Friends\")]'.", query = "PREFIX xyz: <http://sparql.xyz/facade-x/data/> PREFIX fx: <http://sparql.xyz/facade-x/ns/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> CONSTRUCT { ?s ?p ?o . } WHERE { SERVICE <x-sparql-anything:location=https://sparql-anything.cc/example1.json> { fx:properties fx:json.path \"$[?(@.name==\\\"Friends\\\")]\" . ?s ?p ?o } }")
    @Option(description = """
		One or more JsonPath expressions as filters. E.g. `json.path=value` or `json.path.1`, `json.path.2`, `...` to add multiple expressions. The `json.path` option is only recommended if users need to filter a large JSON file, for example, in combination with the `slice` option.\s
		    It will pre-process the JSON before the execution of the query.\s
		    In most cases, it is easier to query the JSON using a triple pattern, as in the [example described before](#Example).""", validValues = "Any valid JsonPath (see [JsonSurfer implementation](https://github.com/jsurfer/JsonSurfer)))")
    public static final IRIArgument PROPERTY_JSONPATH = new IRIArgument("json.path");
    @Option(description = """
		One or more key values as filters. E.g. `json.literalize=key` or `json.literalize.1`, `json.literalize.2`, `...` to add multiple expressions.\s
		The `json.literalize` option is only recommended if users need to treat certain JSON elements as opaque string literals, for example, when using GeoJSON.""",
            validValues = "Any key values present in the JSON file")
    public static final IRIArgument PROPERTY_JSONLITERALIZE = new IRIArgument("json.literalize");

    @Example(resource = "https://sparql-anything.cc/examples/simple.json", description = "Selecting properties having null value.", query = "PREFIX xyz:  <http://sparql.xyz/facade-x/data/> SELECT ?p WHERE { SERVICE <x-sparql-anything:location=https://sparql-anything.cc/examples/simple.json,json.include-null-values=true> { ?s ?p xyz:null } }")
    @Option(description = """
		It tells the JSON triplifier to produce triples for null values in the JSON Object/Array.
		By default the triplifier uses xyz:null as null value.
		See Issue #564.
		""",
            validValues = "true/false")
    public static final IRIArgument PROPERTY_JSONINCLUDENULLVALUES = new IRIArgument("json.include-null-values", "false");

    private static final Logger logger = LoggerFactory.getLogger(JSONTriplifier.class);

    private Set<String> literalKeys = new HashSet<>();

    @Override
    public void triplify(Properties properties, TriplifierEventsHandler eventsHandler) throws IOException, TriplifierHTTPException {
        JsonFactory factory = JsonFactory.builder().build();

        try (InputStream us = Triplifier.getInputStream(properties)) {
            JsonParser parser = factory.createParser(us);
            // Only 1 data source expected
            streamJSON(parser, eventsHandler, PropertyUtils.getBooleanProperty(properties, PROPERTY_JSONINCLUDENULLVALUES));
        }
    }

    private void streamJSON(JsonParser parser, TriplifierEventsHandler eventsHandler, boolean includeNullValues) throws IOException {
        // Start Data Source
        eventsHandler.onDataSource(SPARQLAnythingConstants.DATA_SOURCE_ID);
        eventsHandler.startRoot(SPARQLAnythingConstants.DATA_SOURCE_ID);
        //builder.addRoot(SPARQLAnythingConstants.DATA_SOURCE_ID);
        logger.info("Transforming json (dataSourceId {} rootId {})", SPARQLAnythingConstants.DATA_SOURCE_ID, SPARQLAnythingConstants.ROOT_ID);
        JsonToken token = parser.nextToken();
        if (token == JsonToken.START_OBJECT) {
            logger.info("Transforming object");
            transformObject(parser, SPARQLAnythingConstants.DATA_SOURCE_ID, SPARQLAnythingConstants.ROOT_ID, eventsHandler, includeNullValues);
        } else if (token == JsonToken.START_ARRAY) {
            logger.info("Transforming array");
            transformArray(parser, SPARQLAnythingConstants.DATA_SOURCE_ID, SPARQLAnythingConstants.ROOT_ID, eventsHandler, includeNullValues);
        }
    }

    private void transformArray(JsonParser parser, String dataSourceId, String containerId, TriplifierEventsHandler eventsHandler, boolean includeNullValues) throws IOException {
        int i = 0;
        JsonToken token;

        while ((token = parser.nextToken()) != END_ARRAY) {
            transformArrayItem(i, token, parser, dataSourceId, containerId, eventsHandler, includeNullValues);
            i++;
        }
    }

    private void transformArrayItem(int i, JsonToken token, JsonParser parser, String dataSourceId, String containerId, TriplifierEventsHandler eventsHandler, boolean includeNullValues) throws IOException {

        switch (token) {
            case START_ARRAY -> {
                String childContainerIdArray = StringUtils.join(containerId, "/_", String.valueOf(i + 1));
                eventsHandler.onSlotNumber(i);
                eventsHandler.startContainer(childContainerIdArray);
                //builder.addContainer(dataSourceId, containerId, i + 1, childContainerIdArray);
                transformArray(parser, dataSourceId, childContainerIdArray, eventsHandler, includeNullValues);
                eventsHandler.endContainer();
            }
            case START_OBJECT -> {
                String childContainerId = StringUtils.join(containerId, "/_", String.valueOf(i + 1));
                //builder.addContainer(dataSourceId, containerId, i + 1, childContainerId);
                eventsHandler.onSlotNumber(i);
                eventsHandler.startContainer(childContainerId);
                transformObject(parser, dataSourceId, childContainerId, eventsHandler, includeNullValues);
                eventsHandler.endContainer();
            }
            case VALUE_FALSE, VALUE_TRUE -> eventsHandler.onValue(parser.getValueAsBoolean());
            //builder.addValue(dataSourceId, containerId, i + 1, parser.getValueAsBoolean());
            case VALUE_NUMBER_FLOAT -> //builder.addValue(dataSourceId, containerId, i + 1, parser.getValueAsDouble());
            {
                eventsHandler.onSlotNumber(i);
                eventsHandler.onValue(parser.getValueAsDouble());
            }
            case VALUE_NUMBER_INT -> //builder.addValue(dataSourceId, containerId, i + 1, parser.getValueAsInt());
            {
                eventsHandler.onSlotNumber(i);
                eventsHandler.onValue(parser.getValueAsInt());
            }
            case VALUE_STRING -> //builder.addValue(dataSourceId, containerId, i + 1, parser.getValueAsString());
            {
                eventsHandler.onSlotNumber(i);
                eventsHandler.onValue(parser.getValueAsString());
            }
            case VALUE_NULL -> {
                if (includeNullValues)
                    //builder.addValue(dataSourceId, containerId, i + 1, XYZ_NULL_NODE);
                {
                    eventsHandler.onSlotNumber(i);
                    eventsHandler.onValue(Triplifier.XYZ_NULL_NODE);
                }
            }
            case END_ARRAY, END_OBJECT, FIELD_NAME, VALUE_EMBEDDED_OBJECT, NOT_AVAILABLE -> {
            }
        }
    }

    private void transformObject(JsonParser parser, String dataSourceId, String containerId, TriplifierEventsHandler eventsHandler, boolean includeNullValues) throws IOException {
        JsonToken token;
        Integer coercedInt;
        String coercedStr;

        while ((token = parser.nextToken()) != END_OBJECT) {
            if (token == JsonToken.FIELD_NAME) {
                String k = parser.getText();
                token = parser.nextToken();

                if (literalKeys.contains(k)) {
                    logger.info("Literal key found: {}, next token {}", k, token);
                    eventsHandler.onValue(consumeAsString(parser, token));
                    //builder.addValue(dataSourceId, containerId, k, consumeAsString(parser, token));
                    continue;
                }

                switch (token) {
                    case START_ARRAY -> {
                        String childContainerIdArr = StringUtils.join(containerId, "/", Triplifier.toSafeURIString(k));
                        //builder.addContainer(dataSourceId, containerId, Triplifier.toSafeURIString(k), childContainerIdArr);
                        eventsHandler.onSlotString(k);
                        eventsHandler.startContainer(childContainerIdArr);
                        transformArray(parser, dataSourceId, childContainerIdArr, eventsHandler, includeNullValues);
                        eventsHandler.endContainer();
                    }
                    case START_OBJECT -> {
                        String childContainerId = StringUtils.join(containerId, "/", Triplifier.toSafeURIString(k));
                        //builder.addContainer(dataSourceId, containerId, Triplifier.toSafeURIString(k), childContainerId);
                        eventsHandler.onSlotString(k);
                        eventsHandler.startContainer(childContainerId);
                        transformObject(parser, dataSourceId, childContainerId, eventsHandler, includeNullValues);
                        eventsHandler.endContainer();
                    }
                    case VALUE_NUMBER_FLOAT -> {
                        eventsHandler.onSlotString(k);
                        eventsHandler.onValue(parser.getValueAsDouble());
                        //builder.addValue(dataSourceId, containerId, k, parser.getValueAsDouble());
                    }
                    case VALUE_NUMBER_INT -> {
                        coercedInt = null;
                        coercedStr = null;
                        boolean kIsInteger = true; // assume it is
                        try {
                            coercedInt = parser.getValueAsInt();
                        } catch (Exception e) { // could tighten this to
                            // com.fasterxml.jackson.core.exc.InputCoercionException
                            logger.warn("{} can not be parsed as an integer -- treating it as a string", k);
                            kIsInteger = false;
                            coercedStr = parser.getValueAsString();
                        }
                        eventsHandler.onSlotString(k);
                        eventsHandler.onValue(kIsInteger ? coercedInt : coercedStr);
                        //builder.addValue(dataSourceId, containerId, k, kIsInteger ? coercedInt : coercedStr);
                    }
                    case VALUE_STRING -> //builder.addValue(dataSourceId, containerId, k, parser.getValueAsString());
                            {eventsHandler.onSlotString(k);
                            eventsHandler.onValue(parser.getValueAsString());}
                    case VALUE_FALSE, VALUE_TRUE -> {
                        eventsHandler.onSlotString(k);
                        eventsHandler.onValue(parser.getValueAsBoolean());
                    }
                            //builder.addValue(dataSourceId, containerId, k, parser.getValueAsBoolean());
                    case VALUE_NULL -> {
                        if(includeNullValues){
                            eventsHandler.onSlotString(k);
                            eventsHandler.onValue(Triplifier.XYZ_NULL_NODE);
                        }
                    }
                    case END_ARRAY, END_OBJECT, FIELD_NAME, VALUE_EMBEDDED_OBJECT, NOT_AVAILABLE -> {
                    }
                }
            } else {
                throw new IOException("Unexpected token in object");
            }
        }
    }

    private String consumeAsString(JsonParser parser, JsonToken token) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (token == FIELD_NAME) {
            sb.append(String.format("\"%s\" : ", parser.getText()));
            token = parser.nextToken();
        }
        switch (token) {
            case START_ARRAY -> {
                sb.append(START_ARRAY.asString());
                token = parser.nextToken();
                while (token != END_ARRAY) {
                    sb.append(consumeAsString(parser, token));
                    sb.append(", ");
                    token = parser.nextToken();
                }
                // Remove trailing comma
                sb.delete(sb.length() - 2, sb.length());
                sb.append(END_ARRAY.asString());
            }
            case START_OBJECT -> {
                sb.append(START_OBJECT.asString());
                token = parser.nextToken();
                while (token != END_OBJECT) {
                    sb.append(consumeAsString(parser, token));
                    sb.append(", ");
                    token = parser.nextToken();
                }
                // Remove trailing comma
                sb.delete(sb.length() - 2, sb.length());
                sb.append(END_OBJECT.asString());
            }
            case VALUE_STRING -> sb.append(String.format("\"%s\"", parser.getValueAsString()));
            default -> {
                sb.append(parser.getValueAsString());
            }
        }
        return sb.toString();
    }
}