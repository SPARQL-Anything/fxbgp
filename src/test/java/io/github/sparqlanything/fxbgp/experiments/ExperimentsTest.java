package io.github.sparqlanything.fxbgp.experiments;

import io.github.sparqlanything.fxbgp.Analyser;
import io.github.sparqlanything.fxbgp.AnalyserAsSearch;
import io.github.sparqlanything.fxbgp.AnalyserGrounder;
import io.github.sparqlanything.fxbgp.BGPTestAbstract;
import io.github.sparqlanything.fxbgp.FXModel;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import org.apache.commons.io.IOUtils;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.junit.BeforeClass;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ExperimentsTest extends BGPTestAbstract {
    final protected static Logger L = LoggerFactory.getLogger(ExperimentsTest.class);
    final static URI experimentsFolder;

    static {
        try {
            experimentsFolder = ExperimentsTest.class.getClassLoader().getResource("./experiments/").toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Rule
    public TestName name = new TestName();
    public ExperimentsTest() {
        super(FXModel.getFXModel());
    }
    private static Map<File,Object[]> fileData = new HashMap<>();
    private static Map<Integer,Set<File>> groupedBySize = new HashMap<>();

    private static PrintStream PRINT_EXPERIMENTSCSV_FILE;
    private static PrintStream PRINT_EXPERIMENTS_FILE;
    private static void println(String str){
        System.out.println(str);
        PRINT_EXPERIMENTS_FILE.println(str);
    }

    @BeforeClass
    public static void beforeClass(){
        // Create file EXPERIMENTS.md
        File EXPERIMENTS = new File("./EXPERIMENTS.md");
        File EXPERIMENTSCSV = new File("./EXPERIMENTS.csv");
        EXPERIMENTS.delete();
        EXPERIMENTSCSV.delete();
        try {
            PRINT_EXPERIMENTS_FILE = new PrintStream(new FileOutputStream(EXPERIMENTS));
            PRINT_EXPERIMENTSCSV_FILE = new PrintStream(new FileOutputStream(EXPERIMENTSCSV));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            URI experimentsFolder = ExperimentsTest.class.getClassLoader().getResource("./experiments").toURI();
            files = new File(experimentsFolder).listFiles();
            // Sort files by name
            Arrays.sort(files, new Comparator()
            {
                @Override
                public int compare(Object f1, Object f2) {
                    return ((File) f1).getName().compareTo(((File) f2).getName());
                }
            });

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        println("# Experiments");
        println("## Algorithms");
        println("\nTop down / Search: [AnalyserAsSearch](src/main/java/io/github/sparqlanything/fxbgp/AnalyserAsSearch.java)");
        println("\nBottom up / CSP: [AnalyserGrounder](src/main/java/io/github/sparqlanything/fxbgp/AnalyserGrounder.java)");
        println("");
        println("## Files");
        println("\nEach file tests a single BGP");
        println("\nFile names are informative:");
        println("\n - S: the bgp is satisfiable");
        println(" - N: the bgp is not satisfiable");
        println(" - [number]: the number of triples in the bgp");
        println(" - T: the bgp contains only variables without joins");
        println(" - J: the bgp contains one or more joins");
        println(" - P: the bgp contains a join on an object and must satisfy the single-path-to-root condition");
        println(" - R: the bgp contains a join on an object that is fx:root");
        println(" - C: the bgp contains a join on an object that is a container");
        println("\n\nExamples:");
        println("\n - S_5T: a satisfiable bgp of 5 triples with only variables without joins");
        println(" - E.g. N_3P_C: a not satisfiable bgp of 3 triples that has multiple paths to an object container");
        println("");
        for( File file : files ){
            String name = file.getName();
            String[] aboutArr =  name.split("\\.")[0].split("_");
            Boolean satisfiable = aboutArr[0].equals("S");
            int size = Integer.parseInt(aboutArr[1].substring(0,1));
            String type =  aboutArr[1].substring(1);
            fileData.putIfAbsent(file, new Object[]{satisfiable, size, type});
            groupedBySize.putIfAbsent(size, new HashSet<>());
            groupedBySize.get(size).add(file);
            println("### " + name.replace(".easybgp",""));
            println("\nThe bgp is " + (satisfiable?"":"not ") +"satisfiable." );
            println("\nThe bgp has " + size + " triples");
            if(name.contains("P")){
                println("\nThe bgp has  multiple paths to an object");
            }
            if(name.contains("C")){
                println("\nThe bgp has multiple paths to a container");
            }

            if(name.contains("R")){
                println("\nThe bgp has multiple paths to fx:root");
            }
            println("```");
            String str = null;
            try {
                str = IOUtils.toString(file.toURI(), StandardCharsets.UTF_8.name());
                println(str);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            println("```");
        }
    }

    @Override
    public void before() {
        super.before();
    }

    private void printCSV(String analyser, Boolean complete, String name, Boolean satisfiable, Integer found, String type, Integer size, Long durs, Integer evaluated){
        PRINT_EXPERIMENTSCSV_FILE.println( analyser + "," + complete + "," + name + "," + satisfiable + "," + found + "," + type + "," + size + "," + durs + "," + evaluated );
    }

    private void log(String analyser, boolean complete, String name, Boolean satisfiable, Set<FXBGPAnnotation> anns, String type, Integer size, Long durs, Integer evaluated){
        int found = -1;
        if(anns != null){
            found = anns.size();
        }
        printCSV(analyser, complete, name, satisfiable, found, type, size, durs, evaluated);
        println("| " + name + " | " + satisfiable +" | "+ found + " | " + type + " | " + size + " | " + durs + " | " + evaluated + " |");
    }

    private void thead(){
        println("");
        println("| name | satisfiable? | found | type | size | ms | tested |");
        println("| ---- | ------------ | ----------------- | ---- | ---- | -- | ------ |");
    }
    private void tfoot(){
        println("");
    }

    private static File[] files;

    @Test
    public void TopDown() throws IOException {
        AnalyserAsSearch topDown = new AnalyserAsSearch(properties, FXM());
        println("");
        println("## Top down / Search");

        println("### Top down, only satisfiability");
        println("The algorithm stops when 1 satisfiable annotation is found");
        thead();
        //runAll(topDown, 100, false);
        runWithTimeout(topDown, 5, false);

        //        run(topDown,false, 3, "T", false);
        tfoot();

        println("### Top down, all satisfiable annotations");
        println("The algorithm proceeds to find all possible satisfiable annotations");
        thead();
        runWithTimeout(topDown, 5, true);
        println("");
    }

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
        println("### Bottom up, all annotations (only satisfiable bgps)");
        println("The algorithm proceeds to find all possible satisfiable annotations");
        thead();
        runWithTimeout(bottomUp, 5, true);
        tfoot();
    }

    public void runWithTimeout(final Analyser analyser, int stopAfterSeconds, final boolean complete) throws IOException {
        for(final File file : files){
            // for(Map.Entry<File,Object[]> en: fileData.entrySet()){
            Object[] data = fileData.get(file);
            String name = file.getName().replace(".easybgp","");
            Integer size = (Integer) data[1];
            String type = (String) data[2];
            Boolean satisfiable = (Boolean) data[0];
            if(complete && !satisfiable){
                continue; // Ignore when we look for all solutions of a non satisfiable pattern
            }

            ExecutorService executor = Executors.newCachedThreadPool();
            Callable<Object> task = new Callable<Object>() {
                public Object call() throws IOException {
                    //runInThread(analyser, file, complete);

                    bp = new BasicPattern();
                    readBGP(file.toURI().toURL());
                    long start = System.currentTimeMillis();
                    Set<FXBGPAnnotation> anns = analyser.annotate(new OpBGP(bp()), complete);
                    long  end = System.currentTimeMillis();
                    long durs = end - start;
                    return new Object[]{anns,durs};
                }
            };
            Future<Object> future = executor.submit(task);
            Boolean success = false;
            long durs = 0;
            Set<FXBGPAnnotation> anns = new HashSet<>();
            try {
                Object[] returned = (Object[]) future.get(5, TimeUnit.SECONDS);
                success = true;
                anns = (Set<FXBGPAnnotation> ) returned[0];
                durs = (Long) returned[1];
            } catch (TimeoutException e) {
                success = false;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } finally {
                future.cancel(true);
            }
            if(success){
                if(analyser instanceof AnalyserAsSearch) {
                    log("T", complete, name, satisfiable, anns, type, size, durs, ((AnalyserAsSearch)analyser).getLastIterationsCount());
                }else {
                    log("B", complete, name, satisfiable, anns, type, size, durs, ((AnalyserGrounder)analyser).getLastTestedHypotheses());
//                    println("| " + name + " | " + satisfiable +" | "+ (anns.size()) + " | " + type + " | " + size + " | " + durs + " | " + ((AnalyserGrounder)analyser).getLastTestedHypotheses() + " |");
                }
            }else{
                String analy = "B";
                if(analyser instanceof AnalyserAsSearch) {
                    analy = "T";
                }
                log(analy, complete, name, satisfiable, null, type, size, (long)-1, -1);
            }
        }
    }

    public void runAll(Analyser analyser, Integer onlySizeLowerThan, boolean complete) throws IOException {
        for(File file : files){
        // for(Map.Entry<File,Object[]> en: fileData.entrySet()){
            Object[] data = fileData.get(file);
            String name = file.getName().replace(".easybgp","");
            Integer size = (Integer) data[1];
            String type = (String) data[2];
            Boolean satisfiable = (Boolean) data[0];
            if(complete && !satisfiable){
                continue; // Ignore when we look for all solutions of a non satisfiable pattern
            }
            if(onlySizeLowerThan >= size){
                run(analyser, file, complete);
                if(analyser instanceof AnalyserAsSearch) {
                    println("| " + name + " | " + satisfiable +" | "+ (lastannotations.size()) + " | " + type + " | " + size + " | " + lastDuration + " | " + ((AnalyserAsSearch)analyser).getLastIterationsCount() + " |");
                }else {
                    println("| " + name + " | " + satisfiable +" | "+ (lastannotations.size()) + " | " + type + " | " + size + " | " + lastDuration + " | " + ((AnalyserGrounder)analyser).getLastTestedHypotheses() + " |");
                }
            }else{
                println("| " + name + " | " + satisfiable +" | - | " + type + " | " + size + " | - | - |");
            }
        }
    }

    public void run(Analyser analyser, Boolean satisfiable, Integer size, String type, boolean complete) throws IOException {
        for(Map.Entry<File,Object[]> en: fileData.entrySet()){
            Object[] data = en.getValue();
            File file = (File) en.getKey();
            String name = file.getName().replace(".easybgp","");
            if(satisfiable.equals(data[0]) && size.equals(data[1]) && type.equals(data[2])){
                run(analyser, file, complete);
                if(analyser instanceof AnalyserAsSearch) {
                    println("| " + name + " | " + satisfiable +" | ("+ (lastannotations.size()) + ") | " + type + " | " + size + " | " + lastDuration + "\t(" + ((AnalyserAsSearch)analyser).getLastIterationsCount() + ") |");
                }else {
                    println("| " + name + " | " + satisfiable +" | ("+ (lastannotations.size()) + ") | " + type + " | " + size + " | " + lastDuration + " |");
                }
            }
        }
    }

    private long lastDuration;
    private Set<FXBGPAnnotation> lastannotations;
    public void run(Analyser analyser, File file, boolean complete) throws IOException {
        // Preparation
        bp = new BasicPattern();
        readBGP(file.toURI().toURL());
        long start = System.currentTimeMillis();
        lastannotations = analyser.annotate(new OpBGP(bp()), complete);
        long end = System.currentTimeMillis();
        lastDuration = end - start;
    }
}
