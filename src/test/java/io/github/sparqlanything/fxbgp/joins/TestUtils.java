package io.github.sparqlanything.fxbgp.joins;

import io.github.sparqlanything.fxbgp.*;
import io.github.sparqlanything.fxbgp.stream.join.ContainerSelector;
import io.github.sparqlanything.fxbgp.stream.join.listeners.impl.DataSourceContainerCollectorListenerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.ContainerIsomorphism;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.parsers.CSVParser;
import io.github.sparqlanything.fxbgp.stream.join.parsers.StreamParser;
import io.github.sparqlanything.fxbgp.stream.join.parsers.XMLParser;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.Triplifier;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
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

    private static URL getFileInputURL(String methodName) {
        String file = getMethodNamePart(methodName, 0);
        String format = getMethodNamePart(methodName, 3);
        URL url = BGPTestUtils.class.getClassLoader().getResource("./joins/" + format + "/" + file + "." + format);
        Assert.assertNotNull(url);
        return url;
    }

    private static String getMethodNamePart(String methodName, int x) {
        String file = methodName;
        if (methodName.contains("_")) {
            file = methodName.split("_")[x];
        } else if (x == 3) {
            return "xml";
        }
        return file;
    }

    private static URL getQueryURL(String methodName) {
        String file = getMethodNamePart(methodName, 1);
        URL url = TestUtils.class.getClassLoader().getResource("./joins/queries/" + file + ".easybgp");
        Assert.assertNotNull(url);
        return url;
    }

    private static URL getBindingsURL(String methodName) {
        String file = getMethodNamePart(methodName, 2);
        if (file.equals("noMatch"))
            return null;
        URL url = TestUtils.class.getClassLoader().getResource("./joins/bindings/" + file + ".csv");
        Assert.assertNotNull(url);
        return url;
    }


    public static String getInputFilename(String methodName) throws URISyntaxException {
        URL url = getFileInputURL(methodName);
        return url.toURI().toString();
    }


    public static Set<Map<Node, Node>> getBindings(String methodName) throws URISyntaxException, IOException {
        URL url = getBindingsURL(methodName);
        if (url == null)
            return new HashSet<>();
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
        return getContainerSelectors(methodName, properties, null);
    }

    public static Set<ContainerSelector> getContainerSelectors(String methodName, Properties properties, Set<List<FX>> patterns) throws
            IOException {
        URL url = getQueryURL(methodName);
        BasicPattern basicPattern = BGPTestUtils.readBGP(url);
        OpBGP opBGP = new OpBGP(basicPattern);
        AnalyserGrounder ag = new AnalyserGrounder(properties, FXModel.getFXModel());
        Set<FXBGPAnnotation> annotations = ag.annotate(opBGP, true);
        Set<ContainerSelector> containerSelectors = new HashSet<>();
        for (FXBGPAnnotation annotation : annotations) {
            if (complyWithPatterns(annotation, patterns)) {
                containerSelectors.addAll(ContainerSelector.getSelectors(annotation, properties));
            }
        }

        return containerSelectors;
    }


    private static boolean complyWithPatterns(FXBGPAnnotation annotation, Set<List<FX>> patterns) {
        if (patterns == null)
            return true;

        for (Triple t : annotation.getOpBGP().getPattern().getList()) {
            FX subjectType = annotation.getAnnotation(t.getSubject()).getTerm();
            FX predicateType = annotation.getAnnotation(t.getPredicate()).getTerm();
            FX objectType = annotation.getAnnotation(t.getObject()).getTerm();

            if (!patterns.contains(List.of(subjectType, predicateType, objectType)))
                return false;
        }

        return true;
    }

    private static Node stringToNode(String s) {
        if (s.startsWith("<")) {
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
        } else {
            return NodeFactory.createLiteralString(s);
        }
    }

    public static void assertEquals(String methodName) throws IOException, URISyntaxException {
        assertEquals(methodName, null, true);
    }

    public static void assertEquals(String methodName, Set<List<FX>> triplePatterns) throws IOException, URISyntaxException {
        assertEquals(methodName, triplePatterns, true);
    }

    public static void assertEquals(String methodName, Set<List<FX>> triplePatterns, boolean mustHaveResults, Set<DataSourceContainer> syntheticContainers, Properties propertiesSyntheticContainers) throws IOException, URISyntaxException {
        Set<ContainerSelector> selectors = TestUtils.getContainerSelectors(methodName, propertiesSyntheticContainers, triplePatterns);
        assertEquals(methodName, mustHaveResults, selectors, syntheticContainers);

    }

    public static void assertEquals(String methodName, Set<List<FX>> triplePatterns, boolean mustHaveResults) throws IOException, URISyntaxException {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.LOCATION.toString(), TestUtils.getInputFilename(methodName));

        DataSourceContainerCollectorListenerImpl listener = new DataSourceContainerCollectorListenerImpl();

        String format = getMethodNamePart(methodName, 3);
        StreamParser parser;

        if (format.equals("xml")) {
            parser = new XMLParser(properties, listener);
        } else if (format.equals("csv")) {
            parser = new CSVParser(properties, listener);
        } else {
            throw new UnsupportedOperationException("Unsupported format " + format);

        }
        parser.parse();
        Set<DataSourceContainer> sourceContainers = listener.getCollectedContainers();

        Set<ContainerSelector> selectors = TestUtils.getContainerSelectors(methodName, properties, triplePatterns);

        assertEquals(methodName, mustHaveResults, selectors, sourceContainers);
    }

    private static void assertEquals(String methodName, boolean mustHaveResults, Set<ContainerSelector> selectors, Set<DataSourceContainer> sourceContainers) throws URISyntaxException, IOException {
        Assert.assertFalse(selectors.isEmpty());

        Collection<ContainerIsomorphism> isomorphisms = new HashSet<>();

        for (ContainerSelector selector : selectors) {
            for (DataSourceContainer dataSourceContainer : sourceContainers) {
                Collection<ContainerIsomorphism> bindings = selector.matches(dataSourceContainer);

                if (!mustHaveResults)
                    Assert.assertNull(bindings);

                if (mustHaveResults && bindings != null) {
                    Assert.assertNotNull(bindings);
                    isomorphisms.addAll(bindings);
                }
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



