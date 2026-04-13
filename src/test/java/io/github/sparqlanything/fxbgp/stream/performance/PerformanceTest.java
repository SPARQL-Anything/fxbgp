package io.github.sparqlanything.fxbgp.stream.performance;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import io.github.sparqlanything.engine.FacadeX;
import io.github.sparqlanything.fxbgp.BGPTestUtils;
import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.fxbgp.FXNode;
import io.github.sparqlanything.fxbgp.NodeGenerator;
import io.github.sparqlanything.fxbgp.stream.FXStreamExecutor;
import io.github.sparqlanything.fxbgp.stream.NotATreeException;
import io.github.sparqlanything.model.IRIArgument;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
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
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.github.sparqlanything.fxbgp.stream.performance.CSVGenerator.createRowTypes;

public class PerformanceTest {

    private static final String PERFORMANCE_TEST_INPUT = "performance-test/input";
    private static final String ROW_TYPES_CSV = "rowTypes.csv";
    private static final Random RANDOM = new Random(42);
    //private final static int[] sizes = new int[]{10_000, 100_000, 500_000, 1_000_000, 5_000_000, 10_000_000, 50_000_000, 100_000_000};
    private final static int[] sizes = new int[]{10_000};

    public void prepareCSVInput() throws IOException, URISyntaxException {
        File baseFolder = getBaseFolder();
        System.out.println(baseFolder.getAbsolutePath());
        if (!baseFolder.exists()) {
            baseFolder.mkdirs();
            List<List<String>> rowTypes = createRowTypes(100, 10, 1000, 10, 20);
            CSVGenerator.printCSV(rowTypes, baseFolder.getAbsolutePath() + "/" + ROW_TYPES_CSV);

            for (int size : sizes) {
                System.out.print("Generating size " + size + "...");
                CSVGenerator.generateCSV(size, rowTypes, baseFolder.getAbsolutePath() + "/" + size + ".csv");
                System.out.println("done!");
            }
        }
    }

    private File getBaseFolder() throws URISyntaxException {
        URL baseURL = getClass().getResource(".");
        Assert.assertNotNull(baseURL);
        return new File(new File(baseURL.toURI()), PERFORMANCE_TEST_INPUT);
    }

    public List<List<String>> readRowTypes() throws URISyntaxException, IOException {
        File baseFolder = getBaseFolder();
        CSVParser csvParser = new CSVParser(new FileReader(baseFolder + "/" + ROW_TYPES_CSV), CSVFormat.DEFAULT);
        List<List<String>> rowTypes = new ArrayList<>();
        for (CSVRecord r : csvParser) {
            rowTypes.add(Iter.toList(r.iterator()));
        }
        return rowTypes;
    }

    @Test
    public void testBGP() throws IOException {
        BasicPattern bp = BGPTestUtils.readBGP(new File("/Users/lgu/workspace/SPARQL-Anything/fxbgp/target/test-classes/io/github/sparqlanything/fxbgp/stream/performance/performance-test/input/queries/TP_1/V_1.txt").toURI().toURL());
        System.out.println(bp);
    }

    public void prepareQueries(List<String> rowType) throws URISyntaxException, IOException {

        File baseFolder = getBaseFolder();
        File queriesFolder = new File(baseFolder, "queries");

        if (queriesFolder.exists())
            return;

        FXNode value = new FXNode(FX.SlotString, new NodeGenerator.OrderedValueGenerator(rowType));
        FXNode container = new FXNode(FX.Container, NodeGenerator.variableGenerator);
        FXNode slotNumber = new FXNode(FX.SlotNumber, NodeGenerator.slotNumberGenerator);
        FXNode typeProperty = new FXNode(FX.Type, NodeGenerator.typePropertyGenerator);
        FXNode root = new FXNode(FX.Root, NodeGenerator.rootGenerator);

        BasicPatternGenerator basicPatternGenerator = new BasicPatternGenerator(container, slotNumber, typeProperty, root, value);

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
        prepareCSVInput();
        List<List<String>> rowTypes = readRowTypes();
        prepareQueries(rowTypes.get(RANDOM.nextInt(rowTypes.size())));

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
        OpService opService = new OpService(NodeFactory.createURI("x-sparql-anything:location=" + properties.getProperty(IRIArgument.LOCATION.toString())), op, false);

        boolean streamException = false;
        AtomicLong numOfBindingsStream = new AtomicLong(0L);
        long t0 = System.currentTimeMillis();
        try {
            tl.runWithTimeout(() -> {
                try {
                    numOfBindingsStream.set(executeWithStream(exec, op, properties));
                } catch (NotATreeException e) {
                    throw new RuntimeException("Not a Tree!");
                }
            }, 5, TimeUnit.MINUTES);
        } catch (TimeoutException | InterruptedException e) {
            streamException = true;
        }
        long t1 = System.currentTimeMillis();

        AtomicLong numOfBindingsMaterialisation = new AtomicLong(0L);
        boolean materialisationException = false;
        long t2 = System.currentTimeMillis();
        try {
            tl.runWithTimeout(() -> numOfBindingsMaterialisation.set(executeMaterialisation(opService, execCxt)), 5, TimeUnit.MINUTES);
        } catch (TimeoutException | InterruptedException e) {
            materialisationException = true;
        }
        long t3 = System.currentTimeMillis();

        String stream = streamException ? "T" : String.format("%d", (t1 - t0));
        String materialisation = materialisationException ? "T" : String.format("%d", (t3 - t2));

        System.out.printf("%d\t%d\t%d\t%s\t%s\t%s\t%s\n", size, numOfPatterns, numOfVariables, numOfPredicateVariables, stream, materialisation, numOfBindingsStream.get()!=numOfBindingsMaterialisation.get()?String.format("- %d %d", numOfBindingsStream.get(), numOfBindingsMaterialisation.get()):"");
    }

    private static BasicPattern getBasicPattern(File tpFolder, int numOfVariables, String numberOfVariablesOnPredicates) throws IOException {
        File f = new File(tpFolder, "V_" + numOfVariables + "_" + numberOfVariablesOnPredicates + ".txt");
        if (f.exists())
            return BGPTestUtils.readBGP(f.toURI().toURL());
        return null;
    }

    private long executeMaterialisation(OpService opService, ExecutionContext execCxt) {
        return countResults(QC.execute(opService, OpExecutor.createRootQueryIterator(execCxt), execCxt));
    }

    private long executeWithStream(FXStreamExecutor exec, OpBGP op, Properties properties) throws NotATreeException {
        return countResults(exec.exec(op, properties));
    }

    private long countResults(QueryIterator qi) {
        long l = 0L;
        while (qi.hasNext()) {
            qi.next();
            l++;
        }
        return l;
    }
}
