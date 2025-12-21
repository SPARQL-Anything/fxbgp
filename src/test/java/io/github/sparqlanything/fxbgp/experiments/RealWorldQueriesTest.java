package io.github.sparqlanything.fxbgp.experiments;

import io.github.sparqlanything.fxbgp.Analyser;
import io.github.sparqlanything.fxbgp.AnalyserAsSearch;
import io.github.sparqlanything.fxbgp.AnalyserGrounder;
import io.github.sparqlanything.fxbgp.BGPTestAbstract;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXModel;
import org.apache.commons.io.IOUtils;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.github.sparqlanything.fxbgp.experiments.FXBGPUtils.extract;

public class RealWorldQueriesTest extends BGPTestAbstract {
    final protected static Logger L = LoggerFactory.getLogger(RealWorldQueriesTest.class);
    final static URI experimentsFolder;
    final static Set<FileBGP> opBGPs = new HashSet<>();
    static {
        try {
            experimentsFolder = RealWorldQueriesTest.class.getClassLoader().getResource("./real-world-queries/").toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Rule
    public TestName name = new TestName();
    public RealWorldQueriesTest() {
        super(FXModel.getFXModel());
    }
    private static List<FileBGP> fileData = new ArrayList<>();

    private static PrintStream PRINT_EXPERIMENTSCSV_FILE;
    private static PrintStream PRINT_EXPERIMENTS_FILE;
    private static void println(String str){
        System.out.println(str);
        PRINT_EXPERIMENTS_FILE.println(str);
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        // Create file EXPERIMENTS.md
        File EXPERIMENTS = new File("./REAL-WORLD-QUERIES.md");
        File EXPERIMENTSCSV = new File("./REAL-WORLD-QUERIES.csv");
        EXPERIMENTS.delete();
        EXPERIMENTSCSV.delete();
        try {
            PRINT_EXPERIMENTS_FILE = new PrintStream(new FileOutputStream(EXPERIMENTS));
            PRINT_EXPERIMENTSCSV_FILE = new PrintStream(new FileOutputStream(EXPERIMENTSCSV));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        //URI experimentsFolder = MoreExperimentsTest.class.getClassLoader().getResource("./real-world-queries").toURI();
        files = new File(experimentsFolder).listFiles();
        // Sort files by name
        Arrays.sort(files, new Comparator()
        {
            @Override
            public int compare(Object f1, Object f2) {
                return ((File) f1).getName().compareTo(((File) f2).getName());
            }
        });

        println("# Experiments with real world queries");
        println("## Algorithms");
        println("\nTop down / Search: [AnalyserAsSearch](src/main/java/io/github/sparqlanything/fxbgp/AnalyserAsSearch.java)");
        println("\nBottom up / CSP: [AnalyserGrounder](src/main/java/io/github/sparqlanything/fxbgp/AnalyserGrounder.java)");
        println("");
        println("## Files");
        println("\nEach file may include one or more BGPs");
        println("");
        for( File file : files ){
            fileData.add(FileBGP.make(file));
        }
    }

    @Override
    public void before() {
        super.before();
    }

    private void printCSV(String analyser, Boolean complete, String name, int bgpx, Integer found, int varSize, Integer size, Long durs, Integer evaluated){
        PRINT_EXPERIMENTSCSV_FILE.println( analyser + "," + complete + "," + name + "," + bgpx + "," + found + "," + varSize + "," + size + "," + durs + "," + evaluated );
    }

    private void log(String analyser, boolean complete, String name, int bgpx, Set<FXBGPAnnotation> anns, int varSize, Integer size, Long durs, Integer evaluated){
        int found = -1;
        if(anns != null){
            found = anns.size();
        }
        printCSV(analyser, complete, name, bgpx, found, varSize, size, durs, evaluated);
        println("| " + name + " | " + bgpx +" | "+ found + " | " + varSize + " | " + size + " | " + durs + " | " + evaluated + " |");
    }

    private void thead(){
        println("");
        println("| name | bgpx | found | varSize | size | ms | tested |");
        println("| ---- | ------------ | ----------------- | ---- | ---- | -- | ------ |");
    }
    private void tfoot(){
        println("");
    }

    private static File[] files;

    @Test
    public void BottomUp() throws IOException {
        Analyser bottomUp = new AnalyserGrounder(properties, FXM());
        println("");
        println("## Bottom up / CSP");

        println("### Bottom up, only satisfiability");
        println("The algorithm stops when 1 satisfiable annotation is found");
        thead();
        runWithTimeout(bottomUp, 5, false);

        tfoot();
        println("### Bottom up, all annotations (real queries should have all bgps satisfiable)");
        println("The algorithm proceeds to find all possible satisfiable annotations");
        thead();
        runWithTimeout(bottomUp, 5, true);
        tfoot();
    }

    public void runWithTimeout(final Analyser analyser, int stopAfterSeconds, final boolean complete) throws IOException {
        for(final FileBGP file : fileData){
            if(file.getBgps().isEmpty()){
                L.info("No BGP found / Parsing error {}", file.getName());
                log("B", complete, file.getName(), 0, null, 0, 0, (long) 0, 0);
            }
            int bgpx = 0;
            for(OpBGP opBGP: file.getBgps()) {
                bgpx += 1;
                String name = file.getName();
                Integer size = (Integer) opBGP.getPattern().getList().size();
                Integer varSize = (Integer) FXBGPUtils.varSize(opBGP);
                ExecutorService executor = Executors.newCachedThreadPool();
                Callable<Object> task = new Callable<Object>() {
                    public Object call() throws IOException {
                        // readBGP(file.getFile().toURI().toURL());
                        long start = System.currentTimeMillis();
                        Set<FXBGPAnnotation> anns = analyser.annotate(opBGP, complete);
                        long end = System.currentTimeMillis();
                        long durs = end - start;
                        return new Object[]{anns, durs};
                    }
                };
                Future<Object> future = executor.submit(task);
                Boolean success = false;
                long durs = 0;
                Set<FXBGPAnnotation> anns = new HashSet<>();
                try {
                    Object[] returned = (Object[]) future.get(5, TimeUnit.SECONDS);
                    success = true;
                    anns = (Set<FXBGPAnnotation>) returned[0];
                    durs = (Long) returned[1];
                } catch (TimeoutException e) {
                    success = false;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    //L.error("{} -- Excecution exception: {}", file.getName(), e.getCause());
                    throw new RuntimeException(e);
                } finally {
                    future.cancel(true);
                }
                if (success) {
                    if (analyser instanceof AnalyserAsSearch) {
                        log("T", complete, name, bgpx, anns, varSize, size, durs, ((AnalyserAsSearch) analyser).getLastIterationsCount());
                    } else {
                        log("B", complete, name, bgpx, anns, varSize, size, durs, ((AnalyserGrounder) analyser).getLastTestedHypotheses());
                    }
                } else {
                    String analy = "B";
                    if (analyser instanceof AnalyserAsSearch) {
                        analy = "T";
                    }
                    log(analy, complete, name, bgpx, null, varSize, size, (long) -1, -1);
                }
            }
        }
    }

    @Ignore
    @Test
    public void test244() throws IOException {
        AnalyserGrounder g = new AnalyserGrounder(properties, FXM());
        String qname = "query-244.rq";
        L.info("{}", qname);
        String query = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("./real-world-queries/" + qname), StandardCharsets.UTF_8);
        L.info("{}", query);
        boolean complete = true;
        for(OpBGP bgp : extract(query)) {
            long start = System.currentTimeMillis();
            Set<FXBGPAnnotation> anns = g.annotate(bgp, complete);
            long end = System.currentTimeMillis();
            long durs = end - start;
        }
    }
}
