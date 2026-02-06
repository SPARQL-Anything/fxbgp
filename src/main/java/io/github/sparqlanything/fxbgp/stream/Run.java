package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.BGPTestUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.resultset.ResultSetWriter;
import org.apache.jena.riot.resultset.ResultSetWriterRegistry;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Run {

    public static void main(String[] args) throws IOException, NotATreeException {
        String bgp = args[0];
        File input = new File(args[1]);
        String output = null;
        String props = null;
        if (args.length > 2) {
            props = args[2];
        }
        if (args.length == 4) {
            output = args[3];
        }

        PrintStream out = System.out;
        if (output!= null) {
            try {
                out = new PrintStream(output);
            } catch (Exception e) {
                System.err.println("ERROR. Could not open file!");
                return;
            }
        }

        FXStreamExecutor2 exec = new FXStreamExecutor2();
        Properties properties = new Properties();
        if (props!= null) {
            for(String prop : props.split(";")){
                String[] keyValue = prop.split("=");
                if(keyValue.length == 2){
                    properties.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
        }
        properties.setProperty("location", input.toURI().toString());
        String mediaType = null;
        if(input.getPath().endsWith(".csv")){
            mediaType = "text/csv";
        }else if(input.getPath().endsWith(".json")){
            mediaType = "application/json";
        }
        properties.setProperty("media-type", mediaType);

        BasicPattern bp = BGPTestUtils.readBGP(new File(bgp).toURI().toURL());
        QueryIterator qi = exec.exec(new OpBGP(bp), properties);

        List<String> vars = new ArrayList<>();
        for(Triple t: bp.getList()){
            for(Node n: new Node[]{t.getSubject(),t.getPredicate(),t.getObject()}) {
                if (n.isVariable()) {
                    vars.add(n.getName());
                }
            }
        }

        ResultSetWriterRegistry.init();
        ResultSet rs = ResultSetFactory.create(qi, vars);
        ResultSetFormatter.outputAsCSV(out, rs);

    }
}
