package io.github.sparqlanything.fxbgp.stream;


import io.github.sparqlanything.csv.CSVTriplifier;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.TriplifierHTTPException;
import org.apache.jena.graph.Node;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Properties;

public class ParserEventListenerTest {
    final protected static Logger L = LoggerFactory.getLogger(ParserEventListenerTest.class);

    @Rule
    public TestName name = new TestName();

    @Test
    public void test1() throws IOException, TriplifierHTTPException {
        String n = name.getMethodName();

        FXNodeEventListener listener = new FXAbstractNodeEventListener() {
            public void startContainer(Node container) {
                 L.info("startContainer: {}", container);
            }

            @Override
            public void onValue(Node value) {
                L.info("onValue: {}", value);
            }

            @Override
            public void onType(Node type) {
                L.info("onType: {}", type);
            }

            @Override
            public void onSlotNumber(Node predicate) {
                L.info("onSlotNumber: {}", predicate);
            }

            @Override
            public void onSlotString(Node predicate) {
                L.info("onSlotString: {}", predicate);
            }

            @Override
            public void onTypeRoot() {
                L.info("onTypeRoot");
            }

            @Override
            public void onTypeProperty() {
                L.info("onTypeProperty");
            }

            @Override
            public void endContainer() {
                L.info("endContainer");
            }
        };
        Properties properties = new Properties();
        String location = getClass().getClassLoader().getResource("stream/" + n + ".csv").getPath();
        properties.setProperty(IRIArgument.LOCATION.toString(), location);
        properties.setProperty(CSVTriplifier.PROPERTY_HEADERS.toString(), "true");
        CSVStreamParser parser = new CSVStreamParser(properties);
        FXParserQueryIterator qi = new FXParserQueryIterator(parser, new StreamEventsHandler(properties, listener), Collections.emptySet());
        int events = 0;
        while(qi.hasNext()) {

        }

    }
}
