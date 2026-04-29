package io.github.sparqlanything.fxbgp.stream.performance;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import io.github.sparqlanything.engine.FacadeX;
import io.github.sparqlanything.fxbgp.*;
import io.github.sparqlanything.fxbgp.stream.FXStreamExecutor;
import io.github.sparqlanything.fxbgp.stream.NotATreeException;
import io.github.sparqlanything.model.IRIArgument;
import io.github.sparqlanything.model.Triplifier;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.main.OpExecutor;
import org.apache.jena.sparql.engine.main.QC;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.github.sparqlanything.fxbgp.stream.performance.StringValueGenerator.createRecordTypes;

public class PerformanceTest {

    private static final String PERFORMANCE_TEST_INPUT = "performance-test/input";
    private static final String ROW_TYPES_CSV = "rowTypes.csv";
    private static final Random RANDOM = new Random(42);
    //private final static int[] sizes = new int[]{10_000, 1_000_000, 2_000_000, 3_000_000, 4_000_000, 5_000_000, 10_000_000};
    private final static int[] sizes = new int[]{100_000};
    private static final int NUMBER_OF_COLUMNS = 10;

    public void prepareCSVInput(List<List<String>> rowTypes) throws IOException, URISyntaxException {
        File baseFolder = getBaseFolder();
        System.out.println(baseFolder.getAbsolutePath());
        if (!baseFolder.exists()) {
            baseFolder.mkdirs();
        }


        for (int size : sizes) {
            System.out.print("Generating CSV size " + size + "...");
            CSVGenerator.generateCSV(size, rowTypes, baseFolder.getAbsolutePath() + "/" + size + ".csv");
            System.out.println("done!");

        }
    }

    public void prepareJSONInput(List<List<String>> rowTypes) throws URISyntaxException, IOException {
        // Number of triples CSV = ROWS * COLUMNS + ROWS + 1
        // Number of triples JSON = NUMBER_OF_OBJECTS * NUMBER_OF_SLOTS_OF_OBJECTS + 1

        File baseFolder = getBaseFolder();

        for (int size : sizes) {
            int nodes = size * NUMBER_OF_COLUMNS;
            int height = (int) Math.ceil(Math.log10(nodes));
            System.out.print("Generating JSON size " + size + " of height " + height + " of branching 10...");
            JSONGenerator.generateJSON(height, NUMBER_OF_COLUMNS, rowTypes, String.format("%s/%d_h=%d_k=%d.json", baseFolder.getAbsolutePath(), size, height, NUMBER_OF_COLUMNS));
            System.out.println("done!");

            for (int h = 2; h < height; h++) {
                int k = (int) Math.ceil(Math.pow(nodes, 1 / (double) h));
                System.out.print("Generating JSON size " + size + " of height " + h + " and branching factor " + k + " ...");
                JSONGenerator.generateJSON(h, k, extendRowTypes(rowTypes, k), String.format("%s/%d_h=%d_k=%d.json", baseFolder.getAbsolutePath(), size, h, k));
                System.out.println("done!");
            }
        }

    }


    public void prepareXMLInput(List<List<String>> rowTypes) throws URISyntaxException, IOException, ParserConfigurationException, TransformerException {
        // Number of triples CSV = ROWS * COLUMNS + ROWS + 1
        // Number of triples JSON = NUMBER_OF_OBJECTS * NUMBER_OF_SLOTS_OF_OBJECTS + 1

        File baseFolder = getBaseFolder();

        for (int size : sizes) {
            int nodes = size * NUMBER_OF_COLUMNS;
            int height = (int) Math.ceil(Math.log10(nodes));
            System.out.print("Generating XML size " + size + " of height " + height + " of branching 10...");
            XMLGenerator.generateXML(height, NUMBER_OF_COLUMNS, rowTypes, String.format("%s/%d_h=%d_k=%d.xml", baseFolder.getAbsolutePath(), size, height, NUMBER_OF_COLUMNS));
            System.out.println("done!");

            for (int h = 2; h < height; h++) {
                int k = (int) Math.ceil(Math.pow(nodes, 1 / (double) h));
                System.out.print("Generating XML size " + size + " of height " + h + " and branching factor " + k + " ...");
                XMLGenerator.generateXML(h, k, extendRowTypes(rowTypes, k), String.format("%s/%d_h=%d_k=%d.xml", baseFolder.getAbsolutePath(), size, h, k));
                System.out.println("done!");
            }
        }

    }

    private List<String> extend(List<String> r, int max) {
        List<String> result = new ArrayList<>();
        while (result.size() < max) {
            result.addAll(r);
        }
        return result.subList(0, max);
    }

    private List<List<String>> extendRowTypes(List<List<String>> rowTypes, int k) {
        List<List<String>> result = new ArrayList<>();
        for (List<String> r : rowTypes) {
            result.add(extend(r, k));
        }
        return result;
    }

    private File getBaseFolder() throws URISyntaxException {
        URL baseURL = getClass().getResource(".");
        Assert.assertNotNull(baseURL);
        File result = new File(new File(baseURL.toURI()), PERFORMANCE_TEST_INPUT);
        if (!result.exists())
            result.mkdirs();
        return result;
    }

    public List<List<String>> readRowTypes() throws URISyntaxException, IOException {
        File baseFolder = getBaseFolder();

        if (!new File(baseFolder + "/" + ROW_TYPES_CSV).exists()) {
            List<List<String>> rowTypes = createRecordTypes(100, NUMBER_OF_COLUMNS, 1000, 10, 20);
            CSVGenerator.printCSV(rowTypes, baseFolder.getAbsolutePath() + "/" + ROW_TYPES_CSV);
            return rowTypes;
        }

        CSVParser csvParser = new CSVParser(new FileReader(baseFolder + "/" + ROW_TYPES_CSV), CSVFormat.DEFAULT);
        List<List<String>> rowTypes = new ArrayList<>();
        for (CSVRecord r : csvParser) {
            rowTypes.add(Iter.toList(r.iterator()));
        }
        return rowTypes;
    }

    public void prepareCSVQueries(List<String> rowType) throws URISyntaxException, IOException {

        File baseFolder = getBaseFolder();
        File queriesFolder = new File(baseFolder, "csv_queries/H_1");

        if (queriesFolder.exists())
            return;

        FXNodeGenerator value = new FXNodeGenerator(FX.SlotString, new NodeGenerator.OrderedValueGenerator(rowType));
        FXNodeGenerator container = new FXNodeGenerator(FX.Container, NodeGenerator.variableGenerator);
        FXNodeGenerator slotNumber = new FXNodeGenerator(FX.SlotNumber, NodeGenerator.slotNumberGenerator);
        FXNodeGenerator slotString = new FXNodeGenerator(FX.SlotString, NodeGenerator.slotStringGenerator);
        FXNodeGenerator typeProperty = new FXNodeGenerator(FX.Type, NodeGenerator.typePropertyGenerator);
        FXNodeGenerator root = new FXNodeGenerator(FX.Root, NodeGenerator.rootGenerator);

        BasicPatternGenerator basicPatternGenerator = new BasicPatternGenerator(container, slotNumber, slotString, typeProperty, root, value, null);

        int maxNumOfPatterns = rowType.size();

        queriesFolder.mkdirs();
        for (int numOfPatterns = 1; numOfPatterns <= maxNumOfPatterns; numOfPatterns++) {
            File tpFolder = new File(queriesFolder, "TP_" + numOfPatterns);
            tpFolder.mkdirs();
            for (int numOfVariables = 1; numOfVariables <= numOfPatterns * 2 + 1; numOfVariables++) {
                Set<BasicPattern> bps = basicPatternGenerator.getSxSDistinctNodesWithSlotNumber(numOfPatterns, numOfVariables);

                // 0 Variables on predicates
                Set<BasicPattern> zeroVarsOnPredicates = bps.stream().filter(bp -> testConditionOnNumberOfVariablesInPredicates(bp, n -> n == 0)).collect(Collectors.toSet());

                // 1 Variables on predicates
                Set<BasicPattern> oneVarOnPredicates = bps.stream().filter(bp -> testConditionOnNumberOfVariablesInPredicates(bp, n -> n == 1)).collect(Collectors.toSet());

                // Multiple vars on predicates
                Set<BasicPattern> multipleVarsOnPredicates = bps.stream().filter(bp -> testConditionOnNumberOfVariablesInPredicates(bp, n -> n > 1)).collect(Collectors.toSet());

                System.out.printf("TPs %d Vars %s BPs %d (%d, %d, %d)\n", numOfPatterns, numOfVariables, bps.size(), zeroVarsOnPredicates.size(), oneVarOnPredicates.size(), multipleVarsOnPredicates.size());

                pickRandomAndWriteOnFile(zeroVarsOnPredicates, tpFolder, numOfVariables, String.valueOf(0));
                pickRandomAndWriteOnFile(oneVarOnPredicates, tpFolder, numOfVariables, String.valueOf(1));
                pickRandomAndWriteOnFile(multipleVarsOnPredicates, tpFolder, numOfVariables, "+");
            }
        }
    }

    private static void pickRandomAndWriteOnFile(Set<BasicPattern> bps, File tpFolder, int numOfVariables, String numOfVariablesOnPredicates) throws IOException {
        if (bps.isEmpty()) return;
        BasicPattern chosenPattern = new ArrayList<>(bps).get(RANDOM.nextInt(bps.size()));
        FileWriter fw = new FileWriter(new File(tpFolder, "V_" + numOfVariables + "_" + numOfVariablesOnPredicates + ".txt"));
        IOUtils.write(BGPTestUtils.basicPatternToString(chosenPattern), fw);
        fw.flush();
        fw.close();
    }

    private static void writePatternOnFile(BasicPattern bps, String filepath) throws IOException {
        FileWriter fw = new FileWriter(filepath);
        IOUtils.write(BGPTestUtils.basicPatternToString(bps), fw);
        fw.flush();
        fw.close();
    }

    private static boolean testConditionOnNumberOfVariablesInPredicates(BasicPattern basicPattern, Predicate<Integer> test) {
        int n = 0;
        for (Triple t : basicPattern.getList()) {
            if (t.getPredicate().isVariable()) {
                n++;
            }
        }

        return test.test(n);
    }


    @Test
    public void test1() throws IOException, URISyntaxException {
        List<List<String>> rowTypes = readRowTypes();
        prepareCSVInput(rowTypes);
        prepareCSVQueries(rowTypes.get(RANDOM.nextInt(rowTypes.size())));

        Properties properties = new Properties();
        properties.setProperty(IRIArgument.MEDIA_TYPE.toString(), "text/csv");

        FXStreamExecutor exec = new FXStreamExecutor();

        JenaSystem.init();
        QC.setFactory(ARQ.getContext(), FacadeX.ExecutorFactory);
        ExecutionContext execCxt = ExecutionContext.create(DatasetGraphFactory.create());

        ExecutorService executor = Executors.newCachedThreadPool();
        TimeLimiter tl = SimpleTimeLimiter.create(executor);

        File baseFolder = getBaseFolder();
        int maxNumOfPatterns = rowTypes.size();
        File queriesFolder = new File(baseFolder, "queries");

        System.out.println("Size\t#TPs\t#VARs\t#pVARs\t#TreePatterns\t#StreamSolutions\t#MatSolutions\ttStream\ttMaterialisation\t#SolutionsDiff?");

        //TODO numOfPatterns from 1
        for (int numOfPatterns = 1; numOfPatterns <= maxNumOfPatterns; numOfPatterns++) {
            File tpFolder = new File(queriesFolder, "TP_" + numOfPatterns);
            for (int numOfVariables = 1; numOfVariables <= numOfPatterns * 2 + 1; numOfVariables++) {

                BasicPattern bp0 = getBasicPattern(tpFolder, numOfVariables, "0");
                BasicPattern bp1 = getBasicPattern(tpFolder, numOfVariables, "1");
                BasicPattern bpPlus = getBasicPattern(tpFolder, numOfVariables, "+");

                for (int size : sizes) {
                    String location = baseFolder.getAbsolutePath() + "/" + size + ".csv";
                    properties.setProperty(IRIArgument.LOCATION.toString(), location);

                    executeStreamVsMaterialisation(size, tl, exec, bp0, properties, execCxt, numOfPatterns, numOfVariables, "0");
                    executeStreamVsMaterialisation(size, tl, exec, bp1, properties, execCxt, numOfPatterns, numOfVariables, "1");
                    executeStreamVsMaterialisation(size, tl, exec, bpPlus, properties, execCxt, numOfPatterns, numOfVariables, "+");

                }
            }
        }

    }

    private void executeStreamVsMaterialisation(int size, TimeLimiter tl, FXStreamExecutor exec, BasicPattern bp, Properties properties, ExecutionContext execCxt, int numOfPatterns, int numOfVariables, String numOfPredicateVariables) {
        if (bp == null)
            return;

        OpBGP op = new OpBGP(bp);
        int numSolutionPatterns = computeNumberOfFXBGPAnnotations(properties, op);

        if (numSolutionPatterns > 32) {
            System.out.printf("%d\t%d\t%d\t%s\t%d\n", size, numOfPatterns, numOfVariables, numOfPredicateVariables, numSolutionPatterns);
            return;
        }

        OpService opService = new OpService(NodeFactory.createURI("x-sparql-anything:location=" + properties.getProperty(IRIArgument.LOCATION.toString())), op, false);

        boolean streamException = false;
        AtomicLong numOfBindingsStream = new AtomicLong(0L);
        long t0 = System.currentTimeMillis();
        try {
            tl.runWithTimeout(() -> {
                try {
                    executeWithStream(exec, op, properties, numOfBindingsStream);
                } catch (NotATreeException e) {
                    throw new RuntimeException("Not a Tree!");
                }
            }, 1, TimeUnit.MINUTES);
        } catch (TimeoutException | InterruptedException e) {
            streamException = true;
        }
        long t1 = System.currentTimeMillis();

        AtomicLong numOfBindingsMaterialisation = new AtomicLong(0L);
        boolean materialisationException = false;
        long t2 = System.currentTimeMillis();
        try {
            tl.runWithTimeout(() -> executeMaterialisation(opService, execCxt, numOfBindingsMaterialisation), 5, TimeUnit.MINUTES);
        } catch (TimeoutException | InterruptedException e) {
            materialisationException = true;
        }
        long t3 = System.currentTimeMillis();

        String tStream = streamException ? "T" : String.format("%d", (t1 - t0));
        String tMaterialisation = materialisationException ? "T" : String.format("%d", (t3 - t2));

        System.out.printf("%d\t%d\t%d\t%s\t%d\t%d\t%d\t%s\t%s\t%s\n", size, numOfPatterns, numOfVariables, numOfPredicateVariables, numSolutionPatterns, numOfBindingsStream.get(), numOfBindingsMaterialisation.get(), tStream, tMaterialisation, numOfBindingsStream.get() != numOfBindingsMaterialisation.get() && !tStream.equals("T") && numOfBindingsStream.get() != 0L ? "yes" : "");
    }

    private static BasicPattern getBasicPattern(File tpFolder, int numOfVariables, String numberOfVariablesOnPredicates) throws IOException {
        File f = new File(tpFolder, "V_" + numOfVariables + "_" + numberOfVariablesOnPredicates + ".txt");
        if (f.exists())
            return BGPTestUtils.readBGP(f.toURI().toURL());
        return null;
    }

    private void executeMaterialisation(OpService opService, ExecutionContext execCxt, AtomicLong numOfResults) {
        RunExecutionTests.countResults(QC.execute(opService, OpExecutor.createRootQueryIterator(execCxt), execCxt), numOfResults);
    }

    private void executeWithStream(FXStreamExecutor exec, OpBGP op, Properties properties, AtomicLong numOfResults) throws NotATreeException {
        RunExecutionTests.countResults(exec.exec(op, properties), numOfResults);
    }


    private int computeNumberOfFXBGPAnnotations(Properties properties, OpBGP opBGP) {
        AnalyserGrounder ag = new AnalyserGrounder(properties, FXModel.getFXModel());
        Set<FXBGPAnnotation> annotations = ag.annotate(opBGP, true);
        return annotations.size();
    }


    public void prepareJSONQueries(List<List<String>> rowType, int recordToFind, int height, int branching, int maxNumOfPatterns) throws URISyntaxException, IOException {

        File baseFolder = getBaseFolder();
        File queriesFolder = new File(baseFolder, "json_queries/H=" + height + "_K=" + branching);

        if (!queriesFolder.exists())
            queriesFolder.mkdirs();

        FXNodeGenerator value = new FXNodeGenerator(FX.SlotString, new NodeGenerator.OrderedValueGenerator(rowType.get(recordToFind)));
        FXNodeGenerator container = new FXNodeGenerator(FX.Container, NodeGenerator.variableGenerator);
        FXNodeGenerator slotNumber = new FXNodeGenerator(FX.SlotNumber, NodeGenerator.slotNumberGenerator);
        FXNodeGenerator slotString = new FXNodeGenerator(FX.SlotNumber, NodeGenerator.slotStringGenerator);
        FXNodeGenerator typeProperty = new FXNodeGenerator(FX.Type, NodeGenerator.typePropertyGenerator);
        FXNodeGenerator root = new FXNodeGenerator(FX.Root, NodeGenerator.rootGenerator);

        BasicPatternGenerator basicPatternGenerator = new BasicPatternGenerator(container, slotNumber, slotString, typeProperty, root, value, null);

        for (int numOfPatterns = 1; numOfPatterns <= maxNumOfPatterns; numOfPatterns++) {
            File tpFolder = new File(queriesFolder, "TP_" + numOfPatterns);
            tpFolder.mkdirs();
            for (int numOfVariables = 1; numOfVariables <= numOfPatterns * 2 + 1; numOfVariables++) {

                Set<BasicPattern> bps;
                if (height % 2 == 0) {
                    bps = basicPatternGenerator.getSxSDistinctNodesWithSlotString(numOfPatterns - height + 1, numOfVariables);
                } else {
                    bps = basicPatternGenerator.getSxSDistinctNodesWithSlotNumber(numOfPatterns - height + 1, numOfVariables);
                }

                // 0 Variables on predicates
                Set<BasicPattern> zeroVarsOnPredicates = bps.stream().filter(bp -> testConditionOnNumberOfVariablesInPredicates(bp, n -> n == 0)).collect(Collectors.toSet());
                if (!zeroVarsOnPredicates.isEmpty()) {
                    BasicPattern zeroVarsOnPredicatesPattern = new ArrayList<>(zeroVarsOnPredicates).get(RANDOM.nextInt(zeroVarsOnPredicates.size()));
                    addPathToContainer(zeroVarsOnPredicatesPattern, height, branching, recordToFind);
                    writePatternOnFile(zeroVarsOnPredicatesPattern, String.format("%s/V_%d_0.txt", tpFolder.getAbsolutePath(), numOfVariables));
                }

                // 1 Variables on predicates
                Set<BasicPattern> oneVarOnPredicates = bps.stream().filter(bp -> testConditionOnNumberOfVariablesInPredicates(bp, n -> n == 1)).collect(Collectors.toSet());
                if (!oneVarOnPredicates.isEmpty()) {
                    BasicPattern oneVarOnPredicatesPattern = new ArrayList<>(oneVarOnPredicates).get(RANDOM.nextInt(oneVarOnPredicates.size()));
                    addPathToContainer(oneVarOnPredicatesPattern, height, branching, recordToFind);
                    writePatternOnFile(oneVarOnPredicatesPattern, String.format("%s/V_%d_1.txt", tpFolder.getAbsolutePath(), numOfVariables));
                }

                // Multiple vars on predicates
                System.out.printf("TPs %d Vars %s BPs %d (%d, %d)\n", numOfPatterns, numOfVariables, bps.size(), zeroVarsOnPredicates.size(), oneVarOnPredicates.size());
            }
        }
    }


    public void prepareXMLQueries(List<List<String>> rowType, int recordToFind, int height, int branching, int maxNumOfPatterns) throws URISyntaxException, IOException {

        File baseFolder = getBaseFolder();
        File queriesFolder = new File(baseFolder, "xml_queries/H=" + height + "_K=" + branching + "T");

        if (!queriesFolder.exists())
            queriesFolder.mkdirs();

        Set<String> types = new HashSet<>();
        for (int i = 0; i < height; i++) {
            types.add("type".concat(String.valueOf(i)));
        }

        FXNodeGenerator value = new FXNodeGenerator(FX.SlotString, new NodeGenerator.OrderedValueGenerator(rowType.get(recordToFind)));
        FXNodeGenerator container = new FXNodeGenerator(FX.Container, NodeGenerator.variableGenerator);
        FXNodeGenerator slotNumber = new FXNodeGenerator(FX.SlotNumber, NodeGenerator.slotNumberGenerator);
        FXNodeGenerator slotString = new FXNodeGenerator(FX.SlotNumber, NodeGenerator.slotStringGenerator);
        FXNodeGenerator typeProperty = new FXNodeGenerator(FX.TypeProperty, NodeGenerator.typePropertyGenerator);
        FXNodeGenerator type = new FXNodeGenerator(FX.Type, new NodeGenerator.xyzPredicateGenerator(types));
        FXNodeGenerator root = new FXNodeGenerator(FX.Root, NodeGenerator.rootGenerator);

        BasicPatternGenerator basicPatternGenerator = new BasicPatternGenerator(container, slotNumber, slotString, typeProperty, root, value, type);

        for (int numOfPatterns = 1; numOfPatterns <= maxNumOfPatterns; numOfPatterns++) {
            File tpFolder = new File(queriesFolder, "TP_" + numOfPatterns);
            tpFolder.mkdirs();
            for (int numOfVariables = 1; numOfVariables <= numOfPatterns * 2 + 1; numOfVariables++) {

                Set<BasicPattern> bps = basicPatternGenerator.getSxSDistinctNodesWithSlotStringAndType(numOfPatterns - height + 1, numOfVariables,  Triplifier.XYZ_NS + "type" + (height - 1));

                // 0 Variables on predicates
                Set<BasicPattern> zeroVarsOnPredicates = bps.stream().filter(bp -> testConditionOnNumberOfVariablesInPredicates(bp, n -> n == 0)).collect(Collectors.toSet());
                if (!zeroVarsOnPredicates.isEmpty()) {
                    BasicPattern zeroVarsOnPredicatesPattern = new ArrayList<>(zeroVarsOnPredicates).get(RANDOM.nextInt(zeroVarsOnPredicates.size()));
                    addPathToContainer(zeroVarsOnPredicatesPattern, height, branching, recordToFind);
                    writePatternOnFile(zeroVarsOnPredicatesPattern, String.format("%s/V_%d_0.txt", tpFolder.getAbsolutePath(), numOfVariables));
                }

                // 1 Variables on predicates
                Set<BasicPattern> oneVarOnPredicates = bps.stream().filter(bp -> testConditionOnNumberOfVariablesInPredicates(bp, n -> n == 1)).collect(Collectors.toSet());
                if (!oneVarOnPredicates.isEmpty()) {
                    BasicPattern oneVarOnPredicatesPattern = new ArrayList<>(oneVarOnPredicates).get(RANDOM.nextInt(oneVarOnPredicates.size()));
                    addPathToContainer(oneVarOnPredicatesPattern, height, branching, recordToFind);
                    writePatternOnFile(oneVarOnPredicatesPattern, String.format("%s/V_%d_1.txt", tpFolder.getAbsolutePath(), numOfVariables));
                }

                // Multiple vars on predicates
                System.out.printf("TPs %d Vars %s BPs %d (%d, %d)\n", numOfPatterns, numOfVariables, bps.size(), zeroVarsOnPredicates.size(), oneVarOnPredicates.size());
            }
        }
    }

    public void addPathToContainer(BasicPattern bp, int height, int branching, int containerToFind) {
        Node s = bp.get(0).getSubject();
        Node currentNode = Var.alloc("r");
        int i = 0;
        for (; i < height - 2; i++) {
            int slot = RANDOM.nextInt(branching);
            Node nextNode = Var.alloc("h_" + i + 1);
            if (i % 2 == 0) {
                // slot number
                bp.add(Triple.create(currentNode, RDF.li(slot).asNode(), nextNode));
            } else {
                bp.add(Triple.create(currentNode, NodeFactory.createURI("http://sparql.xyz/facade-x/data/f" + slot), nextNode));
            }
            currentNode = nextNode;
        }

        if (i % 2 == 0) {
            // slot number
            bp.add(Triple.create(currentNode, RDF.li(containerToFind + 1).asNode(), s));
        } else {
            bp.add(Triple.create(currentNode, NodeFactory.createURI("http://sparql.xyz/facade-x/data/f" + containerToFind), s));
        }

    }


    @Test
    public void generateData() throws IOException, URISyntaxException, ParserConfigurationException, TransformerException {
        List<List<String>> rowTypes = readRowTypes();

        prepareCSVInput(rowTypes);
        prepareJSONInput(rowTypes);
        prepareXMLInput(rowTypes);

        int recordToFind = RANDOM.nextInt(rowTypes.size());
        prepareCSVQueries(rowTypes.get(recordToFind));

        for (int size : sizes) {

            int nodes = size * NUMBER_OF_COLUMNS;
            int height = (int) Math.ceil(Math.log10(nodes));
            prepareJSONQueries(rowTypes, recordToFind, height, NUMBER_OF_COLUMNS, NUMBER_OF_COLUMNS);

            for (int h = 2; h < height; h++) {
                int k = (int) Math.ceil(Math.pow(nodes, 1 / (double) h));
                prepareJSONQueries(rowTypes, recordToFind, h, k, NUMBER_OF_COLUMNS);
            }
        }

    }

}
