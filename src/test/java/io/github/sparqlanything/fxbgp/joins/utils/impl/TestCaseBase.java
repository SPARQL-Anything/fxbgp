package io.github.sparqlanything.fxbgp.joins.utils.impl;

import io.github.sparqlanything.fxbgp.*;
import io.github.sparqlanything.fxbgp.joins.TestUtils;
import io.github.sparqlanything.fxbgp.joins.utils.TestCase;
import io.github.sparqlanything.fxbgp.stream.join.ContainerSelector;
import io.github.sparqlanything.fxbgp.stream.join.listeners.impl.DataSourceContainerCollectorListenerImpl;
import io.github.sparqlanything.fxbgp.stream.join.parsers.CSVParser;
import io.github.sparqlanything.fxbgp.stream.join.parsers.StreamParser;
import io.github.sparqlanything.fxbgp.stream.join.parsers.XMLParser;
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

public abstract class TestCaseBase implements TestCase {

    protected abstract String getInputFilepath();

    protected abstract String getQueryFilepath();

    protected abstract String getBindingsFilepath();

    protected abstract String getPatternsFilepath();

    protected abstract String getFormat();

    protected DataSourceContainerCollectorListenerImpl listener = new DataSourceContainerCollectorListenerImpl();

    public DataSourceContainerCollectorListenerImpl getListener() {
        return listener;
    }

    protected static String getMethodNamePart(String methodName, int x) {
        String file = methodName;
        if (methodName.contains("_")) {
            file = methodName.split("_")[x];
        } else if (x == 3) {
            return "xml";
        }
        return file;
    }

    protected URL getFileInputURL() {
        URL url = BGPTestUtils.class.getClassLoader().getResource(getInputFilepath());
        Assert.assertNotNull(url);
        return url;
    }

    protected URL getQueryURL() {
        URL url = TestUtils.class.getClassLoader().getResource(getQueryFilepath());
        Assert.assertNotNull(url);
        return url;
    }

    protected URL getBindingsURL() {
        URL url = TestUtils.class.getClassLoader().getResource(getBindingsFilepath());
        Assert.assertNotNull(url);
        return url;
    }


    protected URL getPatternsURL() {
        URL url = TestUtils.class.getClassLoader().getResource(getPatternsFilepath());
        Assert.assertNotNull(url);
        return url;
    }


    public Set<Map<Node, Node>> getBindings() {
        URL url = getBindingsURL();

        if (url == null)
            return new HashSet<>();

        try {
            return readCSVAsBindings(new File(url.toURI()).getAbsolutePath());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
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

    public BasicPattern getBasicPattern() {
        URL url = getQueryURL();
        try {
            return BGPTestUtils.readBGP(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public StreamParser getParser() {
        Properties properties = getProperties();

        String format = getFormat();
        StreamParser parser;

        if (format.equals("xml")) {
            parser = new XMLParser(properties, listener);
        } else if (format.equals("csv")) {
            parser = new CSVParser(properties, listener);
        } else {
            throw new UnsupportedOperationException("Unsupported format " + format);

        }

        return parser;
    }


    public Set<ContainerSelector> getContainerSelectors() {
        BasicPattern basicPattern = getBasicPattern();
        OpBGP opBGP = new OpBGP(basicPattern);
        AnalyserGrounder ag = new AnalyserGrounder(getProperties(), FXModel.getFXModel());
        Set<FXBGPAnnotation> annotations = ag.annotate(opBGP, true);
        Set<ContainerSelector> containerSelectors = new HashSet<>();
        Set<List<FX>> patterns = getPatterns();
        for (FXBGPAnnotation annotation : annotations) {
            if ((patterns.isEmpty() || complyWithPatterns(annotation, patterns)))
                containerSelectors.addAll(ContainerSelector.getSelectors(annotation, getProperties()));
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

    Set<List<FX>> readTestedPatterns() {
        Set<List<FX>> patterns = new HashSet<>();
        try (Reader reader = new FileReader(new File(getPatternsURL().toURI()));
             org.apache.commons.csv.CSVParser parser = CSVFormat.DEFAULT
                     .parse(reader)) {
            for (CSVRecord record : parser) {
                List<FX> pattern = new ArrayList<>(3);
                record.iterator().forEachRemaining(cell -> pattern.add(stringToFX(cell)));
                patterns.add(pattern);
            }
            return patterns;
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private FX stringToFX(String cell) {
        if (cell.equals("C"))
            return FX.Container;
        else if (cell.equals("SN"))
            return FX.SlotNumber;
        else if (cell.equals("V"))
            return FX.Value;
        else if (cell.equals("SS"))
            return FX.SlotString;
        else if (cell.equals("TP"))
            return FX.TypeProperty;
        else if (cell.equals("T"))
            return FX.Type;
        else if (cell.equals("R"))
            return FX.Root;
        else
            throw new RuntimeException("Unexpected FX type " + cell);

    }


}
