package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.BGPTestUtils;
import org.apache.jena.sparql.core.BasicPattern;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;


public class ToGraphMLTest extends BGPTestUtils {
    Logger L = LoggerFactory.getLogger(ToGraphMLTest.class);
    static TransformerFactory transformerFactory ;
    static Transformer transformer ;
    Document document;
    public static URL  getResource(String name) {
        return ToGraphMLTest.class.getClassLoader().getResource( "/tographml/" + name);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
    }

    @Rule
    public TestName name = new TestName();

    @Test
    public void AT1() throws IOException, ParserConfigurationException, TransformerException {
        process();
        DOMSource source = new DOMSource(document);
        StreamResult target = new StreamResult(System.err);
        transformer.transform(source, target);
    }

    private String outFileName(){
        return name.getMethodName() + ".graphml";
    }

    private void process() throws IOException, ParserConfigurationException, TransformerException {
        // Load BGP
        BasicPattern bp = readBGP(name.getMethodName());
        String outputGraphMLName = outFileName();
        // Generate graphml
        document = ToGraphML.toDocument(bp);
        // Write XML
        DOMSource source = new DOMSource(document);
        FileWriter bpfw = new FileWriter(outputGraphMLName);
        StreamResult target = new StreamResult(bpfw);
        transformer.transform(source, target);
        L.error("written to {}", outputGraphMLName);
    }

    @After
    public void after(){
        new File(outFileName()).delete();
    }
}
