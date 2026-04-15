package io.github.sparqlanything.fxbgp;

import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;
import io.github.sparqlanything.engine.FacadeX;
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

    private static BasicPattern getBasicPattern(File queriesBaseFolder, int numOfTps, int numOfVariables, String numberOfVariablesOnPredicates) throws IOException {
        File tpFolder = new File(queriesBaseFolder, "TP_" + numOfTps);
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

    public static void main(String[] args) throws IOException {

        String testInputBaseFolder = args[0];

        File queryFolder = new File(testInputBaseFolder + "/queries");
        int numOfTps = Integer.parseInt(args[1]);
        int numOfVariables = Integer.parseInt(args[2]);
        String predicateVariables = args[3];
        BasicPattern bp = getBasicPattern(queryFolder, numOfTps, numOfVariables, predicateVariables);
        OpBGP opBGP = new OpBGP(bp);

        int inputSize = Integer.parseInt(args[4]);
        String format = args[5];
        String location = testInputBaseFolder + "/" + inputSize + "." + format;
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.MEDIA_TYPE.toString(), "text/csv");
        properties.setProperty(IRIArgument.LOCATION.toString(), location);

        int numSolutionPatters = computeNumberOfFXBGPAnnotations(properties, opBGP);

        String fxExecutor = args[6];

        ExecutorService executor = Executors.newCachedThreadPool();
        TimeLimiter tl = SimpleTimeLimiter.create(executor);

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
            }, 5, TimeUnit.MINUTES);
        } catch (TimeoutException | InterruptedException e) {
            timeout = true;
        }
        long t1 = System.currentTimeMillis();

        String executionTime = timeout ? "T" : String.valueOf(t1 - t0);

        System.out.printf("%d\t%d\t%d\t%s\t%d\t%d\t%s\t%s\n", inputSize, numOfTps, numOfVariables, predicateVariables, numSolutionPatters, numOfBindings.get(), fxExecutor, executionTime);

        executor.shutdown();
    }

}
