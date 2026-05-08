package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.BGPTestAbstract;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXModel;
import io.github.sparqlanything.fxbgp.experiments.FXBGPUtils;
import io.github.sparqlanything.fxbgp.experiments.FileBGP;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class RealWorldBMATest extends BGPTestAbstract {

    final protected static Logger L = LoggerFactory.getLogger(RealWorldBMATest.class);

    final static URI experimentsFolder;
    static {
        try {
            experimentsFolder = RealWorldBMATest.class.getClassLoader()
                    .getResource("./real-world-queries/").toURI();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<FileBGP> fileData = new ArrayList<>();
    private static File[] files;
    private static PrintStream PRINT_EXPERIMENTS_FILE;

    public RealWorldBMATest() {
        super(FXModel.getFXModel());
    }

    private static void println(String str) {
        System.out.println(str);
        PRINT_EXPERIMENTS_FILE.println(str);
    }

    @BeforeClass
    public static void beforeClass() throws IOException {
        File output = new File("./REAL_WORLD_BMA.md");
        output.delete();
        try {
            PRINT_EXPERIMENTS_FILE = new PrintStream(new FileOutputStream(output));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        files = new File(experimentsFolder).listFiles();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                return f1.getName().compareTo(f2.getName());
            }
        });

        for (File file : files) {
            fileData.add(FileBGP.make(file));
        }

        println("# Experiments with real world queries (BMA)");
        println("");
        println("## Analysis");
        println("");
        println("Each BGP is analysed for BMA structural properties.");
        println("");
        println("## Files");
        println("");
        println("Each file may include one or more BGPs");
        println("");
    }

    @AfterClass
    public static void afterClass() {
        if (PRINT_EXPERIMENTS_FILE != null) {
            PRINT_EXPERIMENTS_FILE.close();
        }
    }

    private void thead() {
        println("");
        println("| name | bgpx | varSize | size | ms | tested | BMA | SingleRoot | Connected | VarInPSize |");
        println("| ---- | ---- | ------- | ---- | -- | ------ | --- | ---------- | --------- | ---------- |");
    }

    private void tfoot() {
        println("");
    }
    private void log(String name, int bgpx,
                     int varSize, Integer size, Long durs, Integer evaluated,
                     boolean bma, boolean singleRoot, boolean connected, int varInPSize) {
        if (bgpx == 0) {
            println("| " + name + " | - | - | - | - | - | - | - | - | - |");
            return;
        }
        println("| " + name
                + " | " + bgpx
                + " | " + varSize
                + " | " + size
                + " | " + durs
                + " | " + evaluated
                + " | " + (bma ? "yes" : "no")
                + " | " + (singleRoot ? "yes" : "no")
                + " | " + (connected ? "yes" : "no")
                + " | " + varInPSize
                + " |");
    }

    @Test
    public void analyse() {
        println("## All BGPs");
        runAnalysis(fileData);

        println("## Queries with a single BGP");
        List<FileBGP> singleBGP = new ArrayList<>();
        for (FileBGP file : fileData) {
            if (file.getBgps().size() == 1) {
                singleBGP.add(file);
            }
        }
        runAnalysis(singleBGP);
    }

    @Test
    public void count() {
        println("## Count queries and BGPs");
        List<FileBGP> singleBGP = new ArrayList<>();
        int bgps = 0;
        int queries = 0;
        for (FileBGP file : fileData) {
            bgps += file.getBgps().size();
            queries += 1;
        }
        System.out.println("# BGPs : " + bgps);
        System.out.println("# Queries : " + queries);
    }

    private void runAnalysis(List<FileBGP> data) {
        int total = 0;
        int countBMA = 0;
        int countSingleRoot = 0;
        int countConnected = 0;
        Map<Integer, Integer> varInPSizeDistribution = new HashMap<>();

        thead();
        for (FileBGP file : data) {
            if (file.getBgps().isEmpty()) {
                log(file.getName(), 0, 0, 0, 0L, 0, false, false, false, 0);
                continue;
            }
            int bgpx = 0;
            for (OpBGP opBGP : file.getBgps()) {
                bgpx++;
                total++;
                BasicPattern bp = opBGP.getPattern();
                int size = bp.getList().size();
                int varSize = FXBGPUtils.varSize(opBGP);
                boolean singleRoot = BMAUtils.singleRoot(bp);
                boolean connected = BMAUtils.connected(bp);
                int varInPSize = BMAUtils.varInPSize(bp);
                boolean bma = new BMAUtils().isBMA(bp);
                if (bma) countBMA++;
                if (singleRoot) countSingleRoot++;
                if (connected) countConnected++;
                varInPSizeDistribution.merge(varInPSize, 1, Integer::sum);
                log(file.getName(), bgpx, varSize, size, 0L, 0,
                        bma, singleRoot, connected, varInPSize);
            }
        }
        tfoot();

        println("### Summary");
        println("");
        println("Total files: " + data.size());
        println("");
        println("");
        println("Total BGPs: " + total);
        println("");
        println("| Category | Count | Percentage |");
        println("| -------- | ----- | ---------- |");
        println("| BMA | " + countBMA + " | " + percent(countBMA, total) + " |");
        println("| SingleRoot | " + countSingleRoot + " | " + percent(countSingleRoot, total) + " |");
        println("| Connected | " + countConnected + " | " + percent(countConnected, total) + " |");
        println("");
        println("#### VarInPSize distribution");
        println("");
        println("| VarInPSize | Count | Percentage |");
        println("| ---------- | ----- | ---------- |");
        final int tot= total;
        varInPSizeDistribution.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> println("| " + e.getKey() + " | " + e.getValue() + " | " + percent(e.getValue(), tot) + " |"));
        println("");
    }

    private static String percent(int count, int total) {
        if (total == 0) return "N/A";
        return String.format("%.1f%%", 100.0 * count / total);
    }

    @AfterClass
    public static void saveSingleBGPQueriesToZip() throws IOException {
        File zipFile = new File("./single-bgp-bma-queries.zip");
        zipFile.delete();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (FileBGP file : fileData) {
                if (file.getBgps().size() == 1 && BMAUtils.isBMA(file.getBgps().get(0).getPattern())) {
                    ZipEntry entry = new ZipEntry(file.getFile().getName());
                    zos.putNextEntry(entry);
                    Files.copy(file.getFile().toPath(), zos);
                    zos.closeEntry();
                }
            }
        }
    }
}