package io.github.sparqlanything.fxbgp.stream;


import io.github.sparqlanything.csv.CSVTriplifier;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.TriplifierHTTPException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class ParserEventListenerTest {
    final protected static Logger L = LoggerFactory.getLogger(ParserEventListenerTest.class);

    @Rule
    public TestName name = new TestName();

    @Test
    public void test1() throws IOException, TriplifierHTTPException {
        String n = name.getMethodName();
        FXNodeEventListener listener = new FXAbstractNodeEventListener() {
        };
        Properties properties = new Properties();
        String location = getClass().getClassLoader().getResource("stream/" + n + ".csv").getPath();
        properties.setProperty(IRIArgument.LOCATION.toString(), location);
        properties.setProperty(CSVTriplifier.PROPERTY_HEADERS.toString(), "true");
        CSVTriplifier2 triplifier2 = new CSVTriplifier2();
        triplifier2.triplify(properties, new StreamEventsHandler(properties, listener));
    }
}
