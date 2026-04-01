package io.github.sparqlanything.fxbgp.experiments;

import io.github.sparqlanything.fxbgp.extractor.BGPExtractor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GTFSBGPExtractor {

    Pattern locationPattern = Pattern.compile("location=(.*?),");

    private String getLocation(String serviceURI) {
        Matcher m = locationPattern.matcher(serviceURI);
        if (m.find()) return serviceURI.substring(m.start(1), m.end(1));
        return null;
    }

    private String toEasyBGP(BasicPattern pattern) {
        return pattern.toString().replaceAll("[()]", "");
    }

    @Test
    public void generateBGPs() throws URISyntaxException, IOException {
        for (String size : new String[]{"1", "10", "100", "1000"}) {
            extract("/Users/lgu/workspace/SPARQL-Anything/experiments/gtfs/csv/" + size + "/", "/Users/lgu/Desktop/queries/csv/" + size);
            extract("/Users/lgu/workspace/SPARQL-Anything/experiments/gtfs/json/" + size + "/", "/Users/lgu/Desktop/queries/json/" + size);
            extract("/Users/lgu/workspace/SPARQL-Anything/experiments/gtfs/xml/" + size + "/", "/Users/lgu/Desktop/queries/xml/" + size);
        }

    }

    public void extract(String in, String out) throws URISyntaxException, IOException {
        System.out.println("IN " + in + " OUT " + out);
        URI experimentsFolder = new File(in).toURI();
        File[] files = new File(experimentsFolder).listFiles();
        File outFolder = new File(out);
        outFolder.mkdirs();
        Assert.assertNotNull(files);
        for (File queryFile : files) {
            String queryString = IOUtils.toString(queryFile.toURI(), Charset.defaultCharset());
            List<String> serviceURIs = new ArrayList<>();
            List<OpBGP> opBGPs = new ArrayList<>();
            FXBGPUtils.extract(queryString, serviceURIs, opBGPs);
            for (int i = 0; i < opBGPs.size(); i++) {
                File locationFile = new File(outFolder, FilenameUtils.getBaseName(queryFile.getName()) + "-" + i + "-location.txt");
                IOUtils.write(getLocation(serviceURIs.get(i)), new FileOutputStream(locationFile), Charset.defaultCharset());
                File bgpFile = new File(outFolder, FilenameUtils.getBaseName(queryFile.getName()) + "-" + i + "-bgp.easybgp");
                IOUtils.write(toEasyBGP(opBGPs.get(i).getPattern()), new FileOutputStream(bgpFile), Charset.defaultCharset());
            }
        }
    }
}
