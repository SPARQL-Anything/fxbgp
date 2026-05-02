package io.github.sparqlanything.fxbgp;

import io.github.sparqlanything.model.IRIArgument;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Sandbox {

    @Test
    public void computeNodesCSV() throws IOException {
        Pattern pattern = Pattern.compile(".*/H_(\\d+)/TP_(\\d+)/V_(\\d+)_([^.]+)\\.txt");
        FileOutputStream fos = new FileOutputStream("/Users/lgu/Desktop/q_info.tsv");

        String[] h = new String[]{ "query_type", "TPs", "VARs", "VARs_ON_P", "SOL_P", "NODES"};

        fos.write(String.join("\t", h).concat("\n").getBytes());


        String format = "csv";
        Properties properties = new Properties();
        if(format.equalsIgnoreCase("csv"))
            properties.setProperty(IRIArgument.MEDIA_TYPE.toString(), "text/csv");
        else if (format.equalsIgnoreCase("json"))
            properties.setProperty(IRIArgument.MEDIA_TYPE.toString(), "application/json");
        else if (format.equalsIgnoreCase("xml"))
            properties.setProperty(IRIArgument.MEDIA_TYPE.toString(), "application/xml");

        try (var stream = Files.walk(new File("/Users/lgu/Desktop/ISWC2026-test/stream-performance/performance-analysis/csv/input_csv/csv_queries").toPath())) {
            stream
                    .filter(Files::isRegularFile)        // only files (remove to include dirs)
                    .filter(p -> p.toString().endsWith(".txt"))  // optional: filter by extension
                    .forEach(p -> {
                        System.out.println("File: " + p);
                        Matcher matcher = pattern.matcher(p.toFile().getAbsolutePath());

                        if (matcher.find()) {
                            String tp      = matcher.group(2); // "4"
                            String vars    = matcher.group(3); // "6"
                            String varsOnP = matcher.group(4); // "+"

                            if(!varsOnP.equals("+")) {

                                try {
                                    BasicPattern bp = getBasicPattern(p.toFile().getAbsolutePath());
                                    OpBGP opBGP = new OpBGP(bp);
                                    int nodes = computeNumberOfNodesInFXBGPAnnotations(properties, opBGP);
                                    int SOL_P = computeNumberOfFXBGPAnnotations(properties, opBGP);

                                    fos.write(String.format("H_1\t%s\t%s\t%s\t%d\t%d\n", tp, vars, varsOnP, nodes, SOL_P).getBytes());

                                } catch (Exception e) {
                                    System.err.println("ERROR! with " + p.toFile().getName());
                                }
                            }



                        } else {
                            System.out.println("No match found.");
                        }

                    });
        }

        fos.flush();
        fos.close();
    }

    @Test
    public void computeTreePatterns() throws IOException {

        BasicPattern bp = getBasicPattern("/Users/lgu/Desktop/ISWC2026-test/stream-performance/performance-analysis/csv/input_csv/csv_queries/H_1/TP_10/V_12_1.txt");
        String format="csv";
        if(bp==null){
            System.out.println("BP null");
            return;
        }

        OpBGP bpg = new OpBGP(bp);
        Properties properties = new Properties();
        if(format.equalsIgnoreCase("csv"))
            properties.setProperty(IRIArgument.MEDIA_TYPE.toString(), "text/csv");
        else if (format.equalsIgnoreCase("json"))
            properties.setProperty(IRIArgument.MEDIA_TYPE.toString(), "application/json");
        else if (format.equalsIgnoreCase("xml"))
            properties.setProperty(IRIArgument.MEDIA_TYPE.toString(), "application/xml");
        System.out.println(computeNumberOfNodesInFXBGPAnnotations(properties, bpg));
        System.out.println(computeNumberOfFXBGPAnnotations(properties, bpg));

    }

    private static int computeNumberOfFXBGPAnnotations(Properties properties, OpBGP opBGP) {
        AnalyserGrounder ag = new AnalyserGrounder(properties, FXModel.getFXModel());
        Set<FXBGPAnnotation> annotations = ag.annotate(opBGP, true);
        return annotations.size();
    }

    private static BasicPattern getBasicPattern(String queryFile) throws IOException {
        File f = new File(queryFile);
        if (f.exists())
            return BGPTestUtils.readBGP(f.toURI().toURL());
        return null;
    }

    private static int computeNumberOfNodesInFXBGPAnnotations(Properties properties, OpBGP opBGP) {
        AnalyserGrounder ag = new AnalyserGrounder(properties, FXModel.getFXModel());
        Set<FXBGPAnnotation> annotations = ag.annotate(opBGP, true);
        return annotations.iterator().next().nodes().size();
    }
}
