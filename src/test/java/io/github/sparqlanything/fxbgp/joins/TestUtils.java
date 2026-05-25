package io.github.sparqlanything.fxbgp.joins;

import io.github.sparqlanything.fxbgp.AnalyserGrounder;
import io.github.sparqlanything.fxbgp.BGPTestUtils;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXModel;
import io.github.sparqlanything.fxbgp.stream.join.ContainerSelector;
import io.github.sparqlanything.fxbgp.stream.join.listeners.impl.DataSourceContainerCollectorListenerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.ContainerIsomorphism;
import io.github.sparqlanything.fxbgp.stream.join.parsers.XMLParser;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.Triplifier;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class TestUtils {

    public static String getInputFilename(String methodName) throws URISyntaxException {
        URL url = BGPTestUtils.class.getClassLoader().getResource("./joins/xml/" + methodName + ".xml");
        Assert.assertNotNull(url);
        return url.toURI().toString();
    }


    public static Set<Map<Node, Node>> getBindings(String methodName) throws URISyntaxException, IOException {
        URL url = TestUtils.class.getClassLoader().getResource("./joins/bindings/" + methodName + ".csv");
        Assert.assertNotNull(url);
        return readCSVAsBindings(new File(url.toURI()).getAbsolutePath());
    }

    private static List<Map<String, String>> readCSV(String filePath) throws IOException {
        try (Reader reader = new FileReader(filePath);
             org.apache.commons.csv.CSVParser parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .parse(reader)) {
            List<String> headers = parser.getHeaderNames();
            List<Map<String, String>> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                Map<String, String> row = new LinkedHashMap<>();
                for (String header : headers) {
                    row.put(header, record.get(header));
                }
                rows.add(row);
            }
            return rows;
        }
    }

    private static Set<Map<Node, Node>> readCSVAsBindings(String filePath) throws IOException {
        try (Reader reader = new FileReader(filePath);
             org.apache.commons.csv.CSVParser parser = CSVFormat.DEFAULT
                     .withFirstRecordAsHeader()
                     .parse(reader)) {
            List<String> headers = parser.getHeaderNames();
            Set<Map<Node, Node>> rows = new HashSet<>();
            for (CSVRecord record : parser) {
                Map<Node, Node> binding = new HashMap<>();
                for (String header : headers) {
                    binding.put(stringToNode(header), stringToNode(record.get(header)));

                }
                rows.add(binding);
            }
            return rows;
        }
    }

    public static Set<ContainerSelector> getContainerSelectors(String methodName, Properties properties) throws
            IOException {
        URL url = TestUtils.class.getClassLoader().getResource("./joins/queries/" + methodName + ".easybgp");
        Assert.assertNotNull(url);
        BasicPattern basicPattern = BGPTestUtils.readBGP(url);
        OpBGP opBGP = new OpBGP(basicPattern);
        AnalyserGrounder ag = new AnalyserGrounder(properties, FXModel.getFXModel());
        Set<FXBGPAnnotation> annotations = ag.annotate(opBGP, true);
        Set<ContainerSelector> containerSelectors = new HashSet<>();
        for (FXBGPAnnotation annotation : annotations) {
            containerSelectors.addAll(ContainerSelector.getSelectors(annotation, properties));
        }

        return containerSelectors;
    }

    private static Node stringToNode(String s) {
        if (s.startsWith("\"")) {
            return NodeFactory.createLiteralString(s.substring(1, s.length() - 1));
        } else if (s.startsWith("<")) {
            return NodeFactory.createURI(s.substring(1, s.length() - 1));
        } else if (s.startsWith("_:")) {
            return NodeFactory.createBlankNode(s.substring(2));
        } else if (s.startsWith("?")) {
            return Var.alloc(s.substring(1));
        } else if (s.startsWith("rdf:")) {
            return NodeFactory.createURI(RDF.type.getNameSpace() + s.substring(4));
        } else if (s.startsWith("xyz:")) {
            return NodeFactory.createURI(Triplifier.XYZ_NS + s.substring(4));
        } else if (s.startsWith("fx:")) {
            return NodeFactory.createURI(Triplifier.FACADE_X_CONST_NAMESPACE_IRI + s.substring(3));
        }
        throw new RuntimeException("Unrecognized format!");
    }


    public static void assertEquals(String methodName) throws IOException, URISyntaxException {

        //System.out.println(TestUtils.getBindings(name.getMethodName()));
        Properties properties = new Properties();

        properties.setProperty(IRIArgument.LOCATION.toString(), TestUtils.getInputFilename(methodName));
        DataSourceContainerCollectorListenerImpl listener = new DataSourceContainerCollectorListenerImpl();
        XMLParser xmlParser = new XMLParser(properties, listener);
        xmlParser.parse();
        Set<DataSourceContainer> sourceContainers = listener.getCollectedContainers();

        Set<ContainerSelector> selectors = TestUtils.getContainerSelectors(methodName, properties);
        Collection<ContainerIsomorphism> isomorphisms = new HashSet<>();

        for (ContainerSelector selector : selectors) {
            for (DataSourceContainer dataSourceContainer : sourceContainers) {
                Collection<ContainerIsomorphism> bindings = selector.matches(dataSourceContainer);
                Assert.assertNotNull(bindings);
                isomorphisms.addAll(bindings);
            }
        }

        Set<Map<Node, Node>> expectedBindings = TestUtils.getBindings(methodName);
        Set<Map<Node, Node>> actualBindings = new HashSet<>();
        for (ContainerIsomorphism containerIsomorphism : isomorphisms) {
            actualBindings.add(containerIsomorphism.asMap());
        }

        Assert.assertEquals(expectedBindings, actualBindings);

    }


}



