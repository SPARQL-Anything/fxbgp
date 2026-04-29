package io.github.sparqlanything.fxbgp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import io.github.sparqlanything.engine.FacadeX;
import io.github.sparqlanything.engine.Utils;
import io.github.sparqlanything.fxbgp.stream.FXStreamExecutor;
import io.github.sparqlanything.fxbgp.stream.NotATreeException;
import io.github.sparqlanything.model.IRIArgument;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sys.JenaSystem;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class RunExecutionTests {

    private static void executeWithStream(OpBGP op, Properties properties, AtomicLong numOfResults) throws NotATreeException {
        FXStreamExecutor exec = new FXStreamExecutor();
        countResults(exec.exec(op, properties), numOfResults);
    }

    private static void executeMaterialisation(OpBGP op, Properties properties, AtomicLong numOfResults) {
        JenaSystem.init();
        QC.setFactory(ARQ.getContext(), FacadeX.ExecutorFactory);
        ExecutionContext execCxt = ExecutionContext.create(DatasetGraphFactory.create());
        OpService opService = new OpService(NodeFactory.createURI("x-sparql-anything:location=" + properties.getProperty(IRIArgument.LOCATION.toString())), op, false);
        countResults(QC.execute(opService, OpExecutor.createRootQueryIterator(execCxt), execCxt), numOfResults);
    }

    private static void executeMaterialisationTest(OpBGP op, Properties properties) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        String location =  properties.getProperty(IRIArgument.LOCATION.toString());
        JsonNode rootNode = mapper.readTree(new File(location));
        System.out.println(rootNode.get(30).get("f0"));
        JenaSystem.init();
        QC.setFactory(ARQ.getContext(), FacadeX.ExecutorFactory);
        ExecutionContext execCxt = ExecutionContext.create(DatasetGraphFactory.create());
        OpService opService = new OpService(NodeFactory.createURI("x-sparql-anything:location=" +location), op, false);
        QueryIterator queryIterator = QC.execute(opService, OpExecutor.createRootQueryIterator(execCxt), execCxt);
        System.out.println(Utils.queryIteratorToString(queryIterator));
    }


    private static void executeMaterialisationTestXML(OpBGP op, Properties properties) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {

        String location =  properties.getProperty(IRIArgument.LOCATION.toString());

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(location);

        doc.getDocumentElement().normalize();

        Node n = doc.getDocumentElement().getChildNodes().item(16).getChildNodes().item(0).getChildNodes().item(30);

        System.out.println(((Element)n).getAttribute("f0"));
        System.out.println(((Element)n).getAttribute("f1"));
        System.out.println(((Element)n).getAttribute("f2"));
        System.out.println(location);

        XPath xpath = XPathFactory.newInstance().newXPath();

        NodeList nodes = (NodeList) xpath.evaluate(
                "//*[@f0='gKUfWDfaEqCsoFjzjQ']",
                doc,
                XPathConstants.NODESET
        );

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            System.out.println(getXPath(node));
        }

        JenaSystem.init();
        QC.setFactory(ARQ.getContext(), FacadeX.ExecutorFactory);
        ExecutionContext execCxt = ExecutionContext.create(DatasetGraphFactory.create());
        OpService opService = new OpService(NodeFactory.createURI("x-sparql-anything:location=" +location), op, false);
        QueryIterator queryIterator = QC.execute(opService, OpExecutor.createRootQueryIterator(execCxt), execCxt);
        System.out.println(Utils.queryIteratorToString(queryIterator));
    }

    private static BasicPattern getBasicPattern(File queriesBaseFolder, String graphPatternType, int numOfTps, int numOfVariables, String numberOfVariablesOnPredicates) throws IOException {
        File dFolder = new File(queriesBaseFolder, graphPatternType);
        File tpFolder = new File(dFolder, "TP_" + numOfTps);
        File f = new File(tpFolder, "V_" + numOfVariables + "_" + numberOfVariablesOnPredicates + ".txt");
        if (f.exists())
            return BGPTestUtils.readBGP(f.toURI().toURL());
        return null;
    }

    public static void countResults(QueryIterator qi, AtomicLong numOfResults) {
        while (qi.hasNext()) {
            qi.next();
            numOfResults.incrementAndGet();
        }
    }

    private static int computeNumberOfFXBGPAnnotations(Properties properties, OpBGP opBGP) {
        AnalyserGrounder ag = new AnalyserGrounder(properties, FXModel.getFXModel());
        Set<FXBGPAnnotation> annotations = ag.annotate(opBGP, true);
        return annotations.size();
    }

    private static int computeNumberOfNodesInFXBGPAnnotations(Properties properties, OpBGP opBGP) {
        AnalyserGrounder ag = new AnalyserGrounder(properties, FXModel.getFXModel());
        Set<FXBGPAnnotation> annotations = ag.annotate(opBGP, true);
        return annotations.iterator().next().nodes().size();
    }

    public static String getXPath(Node node) {
        if (node == null)
            return null;

        if (node.getNodeType() == Node.DOCUMENT_NODE)
            return "";

        StringBuilder path = new StringBuilder();

        while (node != null && node.getNodeType() != Node.DOCUMENT_NODE) {

            int index = 1;
            Node sibling = node.getPreviousSibling();

            while (sibling != null) {
                if (sibling.getNodeType() == Node.ELEMENT_NODE &&
                        sibling.getNodeName().equals(node.getNodeName())) {
                    index++;
                }
                sibling = sibling.getPreviousSibling();
            }

            path.insert(0, "/" + node.getNodeName() + "[" + index + "]");
            node = node.getParentNode();
        }

        return path.toString();
    }

    public static void main(String[] args) throws IOException {

        System.setProperty("jdk.xml.elementAttributeLimit", "0");

        String testInputBaseFolder = args[0];
        String format = args[6];

        File queryFolder = new File(testInputBaseFolder + String.format("/%s_queries", format));

        String graphPatternType = args[1];
        int numOfTps = Integer.parseInt(args[2]);
        int numOfVariables = Integer.parseInt(args[3]);
        String predicateVariables = args[4];
        BasicPattern bp = getBasicPattern(queryFolder, graphPatternType, numOfTps, numOfVariables, predicateVariables);
        int inputSize = Integer.parseInt(args[5]);
        String fxExecutor = args[7];
        if(bp==null){
            System.out.printf("%d\t%s\t%s\t%d\t%d\t%s\t%d\t%d\t%d\t%s\t%s\n", inputSize, format, graphPatternType, numOfTps, numOfVariables, predicateVariables, 0, 0,0, fxExecutor, "-");
            return;
        }
        OpBGP opBGP = new OpBGP(bp);

        String patternType = format.equalsIgnoreCase("json") || format.equalsIgnoreCase("xml") ? "_" + graphPatternType : "";
        String location = testInputBaseFolder + "/" + inputSize + patternType + "." + format;
        Properties properties = new Properties();

        if(format.equalsIgnoreCase("csv"))
            properties.setProperty(IRIArgument.MEDIA_TYPE.toString(), "text/csv");
        else if (format.equalsIgnoreCase("json"))
            properties.setProperty(IRIArgument.MEDIA_TYPE.toString(), "application/json");
        else if (format.equalsIgnoreCase("xml"))
            properties.setProperty(IRIArgument.MEDIA_TYPE.toString(), "application/xml");

        properties.setProperty(IRIArgument.LOCATION.toString(), location);

        int numSolutionPatters = computeNumberOfFXBGPAnnotations(properties, opBGP);

        int numOfNodesInTreePatterns = computeNumberOfNodesInFXBGPAnnotations(properties, opBGP);

        int timeoutLimit = Integer.parseInt(args[8]);

        ExecutorService executor = Executors.newCachedThreadPool();
        TimeLimiter tl = SimpleTimeLimiter.create(executor);


        try {
            executeMaterialisationTestXML(opBGP, properties);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }


        boolean timeout = false;
        AtomicLong numOfBindings = new AtomicLong(0L);
        long t0 = System.currentTimeMillis();
        try {
            tl.runWithTimeout(() -> {
                if (fxExecutor.equalsIgnoreCase("stream")) {
                    try {
                        executeWithStream(opBGP, properties, numOfBindings);
                    } catch (NotATreeException e) {
                        System.err.println("Not a tree!");
                    }
                } else if (fxExecutor.equalsIgnoreCase("materialisation")) {
                    executeMaterialisation(opBGP, properties, numOfBindings);
                }
            }, timeoutLimit, TimeUnit.MINUTES);
        } catch (TimeoutException | InterruptedException e) {
            timeout = true;
        }
        long t1 = System.currentTimeMillis();

        String executionTime = timeout ? "T" : String.valueOf(t1 - t0);

        System.out.printf("%d\t%s\t%s\t%d\t%d\t%s\t%d\t%d\t%d\t%s\t%s\n", inputSize, format, graphPatternType, numOfTps, numOfVariables, predicateVariables, numSolutionPatters, numOfNodesInTreePatterns, numOfBindings.get(), fxExecutor, executionTime);

        executor.shutdown();
    }

}
