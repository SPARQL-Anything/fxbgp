package io.github.sparqlanything.fxbgp.experiments;

import io.github.sparqlanything.fxbgp.Analyser;
import io.github.sparqlanything.fxbgp.AnalyserAsSearch;
import io.github.sparqlanything.fxbgp.AnalyserGrounder;
import io.github.sparqlanything.fxbgp.BGPTestAbstract;
import io.github.sparqlanything.fxbgp.FXModel;
import io.github.sparqlanything.fxbgp.InterpretationOfBGP;
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

public class Experiments extends BGPTestAbstract {
    final protected static Logger L = LoggerFactory.getLogger(Experiments.class);

    @Rule
    public TestName name = new TestName();
    public Experiments() {
        super(FXModel.getFXModel());
    }
    private static Map<File,Object[]> fileData = new HashMap<>();
    private static Map<Integer,Set<File>> groupedBySize = new HashMap<>();

    private static PrintStream PRINT_EXPERIMENTS_FILE;
    private static void println(String str){
        System.out.println(str);
        PRINT_EXPERIMENTS_FILE.println(str);
    }

    @BeforeClass
    public static void beforeClass(){
        // Create file EXPERIMENTS.md
        File EXPERIMENTS = new File("./EXPERIMENTS.md");
        EXPERIMENTS.delete();
        try {
            PRINT_EXPERIMENTS_FILE = new PrintStream(new FileOutputStream(EXPERIMENTS));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            URI experimentsFolder = Experiments.class.getClassLoader().getResource("./experiments").toURI();
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

        println("# Experiment files");
        for( File file : files ){
            String name = file.getName();
            String[] aboutArr =  name.split("\\.")[0].split("_");
            Boolean satisfiable = aboutArr[0].equals("S");
            int size = Integer.parseInt(aboutArr[1].substring(0,1));
            String type =  aboutArr[1].substring(1);
            fileData.putIfAbsent(file, new Object[]{satisfiable, size, type});
            groupedBySize.putIfAbsent(size, new HashSet<>());
            groupedBySize.get(size).add(file);
            println("## File " + name.replace(".easybgp",""));
            println("Satisfiable: " + satisfiable);
            println("Number of triples: " + size);
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

    private static File[] files;

    @Test
    public void TopDown() throws IOException {
        AnalyserAsSearch topDown = new AnalyserAsSearch(properties, FXM());
        Analyser bottomUp = new AnalyserGrounder(properties, FXM());
        println("");

        println("## Top down, only satisfiability");
        println("");

        println("| name | sat (ann) | type | size | ms |");
        println("| ---- | --------- | ---- | ---- | -- |");
        run(topDown,true, 1, "T", false);
        run(topDown,true, 2, "T", false);
        run(topDown, true, 3, "T", false);
        run(topDown, true, 4, "T", false);
        run(topDown, true, 5, "T", false);
        run(topDown,false, 1, "T", false);
        run(topDown,false, 2, "T", false);
//        run(topDown,false, 3, "T", false);
        println("");
        println("## Top down, all satisfiable annotations");
        println("");
        println("| name | sat (ann) | type | size | ms |");
        println("| ---- | --------- | ---- | ---- | -- |");
        run(topDown,true, 1, "T", true);
        run(topDown,true, 2, "T", true);
        //run(topDown, true, 3, "T", true);

        println("");

        //run(topDown, false, 3, "T", false);
        //run(topDown, false, 4, "T", false);
        //run(topDown, false, 5, "T", false);
        //println("## Top down, all satisfiable annotations");
        //println("name\tsatisfiable\ttype\tsize\tms");
        //run(topDown,false, 1, "T", true);
        //run(topDown,false, 2, "T", true);
        //run(topDown, true, 3, "T", true);
    }

    @Test
    public void BottomUp() throws IOException {
        Analyser bottomUp = new AnalyserGrounder(properties, FXM());
        println("");
        println("## Bottom up, only satisfiability");
        println("");
        println("| name | sat (ann) | type | size | ms |");
        println("| ---- | --------- | ---- | ---- | -- |");
        run(bottomUp,true, 1, "T", false);
        run(bottomUp,true, 2, "T", false);
        run(bottomUp, true, 3, "T", false);
        run(bottomUp, true, 4, "T", false);
        run(bottomUp, true, 5, "T", false);

        run(bottomUp,false, 1, "T", false);
        run(bottomUp,false, 2, "T", false);
        run(bottomUp, false, 3, "T", false);
        run(bottomUp, false, 4, "T", false);
        run(bottomUp, false, 5, "T", false);

        run(bottomUp, true, 2, "J", false);
        run(bottomUp, true, 3, "J", false);
        run(bottomUp, true, 4, "J", false);

        run(bottomUp, false, 2, "J", false);
        run(bottomUp, false, 3, "J", false);
        run(bottomUp, false, 4, "J", false);
        run(bottomUp, false, 5, "J", false);

        run(bottomUp, true, 3, "P", false);
        run(bottomUp, true, 4, "P", false);
        run(bottomUp, true, 5, "P", false);

        run(bottomUp, false, 3, "P", false);
        run(bottomUp, false, 4, "P", false);
        run(bottomUp, false, 5, "P", false);
        println("");
        println("## Bottom up, all annotations (only satisfiable bgps)");
        println("");
        println("| name | sat (ann) | type | size | ms |");
        println("| ---- | --------- | ---- | ---- | -- |");
        run(bottomUp,true, 1, "T", true);
        run(bottomUp,true, 2, "T", true);
        run(bottomUp,true, 3, "T", true);
        run(bottomUp,true, 4, "T", true);
        run(bottomUp,true, 5, "T", true);

        run(bottomUp, true, 2, "J", true);
        run(bottomUp, true, 3, "J", true);
        run(bottomUp, true, 4, "J", true);

        run(bottomUp, true, 3, "P", true);
        run(bottomUp, true, 4, "P", true);
        run(bottomUp, true, 5, "P", true);
        println("");
    }

    public void run(Analyser analyser, Boolean satisfiable, Integer size, String type, boolean complete) throws IOException {
        for(Map.Entry<File,Object[]> en: fileData.entrySet()){
            Object[] data = en.getValue();
            File file = (File) en.getKey();
            String name = file.getName().replace(".easybgp","");
            if(satisfiable.equals(data[0]) && size.equals(data[1]) && type.equals(data[2])){
                run(analyser, file, complete);
                if(analyser instanceof AnalyserAsSearch) {
                    println("| " + name + " | " + satisfiable +" | ("+ (lastInterpretations.size()) + ") | " + type + " | " + size + " | " + lastDuration + "\t(" + ((AnalyserAsSearch)analyser).getLastIterationsCount() + ") |");
                }else {
                    println("| " + name + " | " + satisfiable +" | ("+ (lastInterpretations.size()) + ") | " + type + " | " + size + " | " + lastDuration + " |");
                }
            }
        }
    }

    private long lastDuration;
    private Set<InterpretationOfBGP> lastInterpretations;
    public void run(Analyser analyser, File file, boolean complete) throws IOException {
        // Preparation
        bp = new BasicPattern();
        readBGP(file.toURI().toURL());
        long start = System.currentTimeMillis();
        lastInterpretations = analyser.interpret(new OpBGP(bp()), complete);
        long end = System.currentTimeMillis();
        lastDuration = end - start;
    }
}
