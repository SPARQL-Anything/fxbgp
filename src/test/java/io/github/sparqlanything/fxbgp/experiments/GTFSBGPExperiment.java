package io.github.sparqlanything.fxbgp.experiments;

import io.github.sparqlanything.csv.CSVTriplifier;
import io.github.sparqlanything.engine.FacadeX;
import io.github.sparqlanything.fxbgp.BGPTestUtils;
import io.github.sparqlanything.fxbgp.stream.FXStreamExecutor;
import io.github.sparqlanything.fxbgp.stream.NotATreeException;
import io.github.sparqlanything.model.IRIArgument;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
import org.apache.jena.sparql.algebra.Algebra;
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

public class GTFSBGPExperiment {

    @Test
    public void algebra() {
        String q = "PREFIX fx:  <http://sparql.xyz/facade-x/ns/>\n" +
                "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX xyz: <http://sparql.xyz/facade-x/data/>\n" +
                "SELECT ?stop ?parStation ?accName ?name WHERE {\n" +
                "\n" +
                "  # stops\n" +
                "\tSERVICE <x-sparql-anything:location=../result/datasets/%format/%size/STOPS.%format,%param,slice=%slice,strategy=%strategy> {\n" +
                "    ?stopContainer xyz:stop_id ?stop_id .\n" +
                "    ?stopContainer xyz:parent_station ?parent_station .\n" +
                "\n" +
                "    OPTIONAL {?stopContainer xyz:stop_name ?accName} .\n" +
                "\n" +
                "    ?stopContainer xyz:location_type \"2\" .\n" +
                "\n" +
                "    ?stopContainerParStation xyz:stop_name ?name .\n" +
                "    ?stopContainerParStation  xyz:stop_id ?parent_station .\n" +
                "\n" +
                "    BIND ( fx:entity( \"http://transport.linkeddata.es/madrid/metro/stops/\", ?stop_id ) AS ?stop ) .\n" +
                "    BIND ( fx:entity( \"http://transport.linkeddata.es/madrid/metro/stops/\", ?parent_station ) AS ?parStation ) .\n" +
                "\t}\n" +
                "\n" +
                "}";

        System.out.println(Algebra.optimize(Algebra.compile(QueryFactory.create(q))));
    }


    @Test
    public void experimentsCSV() throws IOException {
        String[] sizes = new String[]{"1", "10", "100", "1000"};
        String format = "csv";
        String resultFolder = "/Users/lgu/Desktop/experiment";

        for (String size : sizes) {
            String queryFolderPath = String.format("/Users/lgu/Desktop/experiment/queries/%s/%s", format, size);
            Properties properties = new Properties();
            properties.setProperty(CSVTriplifier.PROPERTY_HEADERS.toString(), "true");
            properties.setProperty(IRIArgument.MEDIA_TYPE.toString(), "text/csv");
            experiment(queryFolderPath, resultFolder, properties, size);
        }

    }

    private void experiment(String queryFolderPath, String resultFolder, Properties properties, String size) throws IOException {
        File queryFolderFile = new File(queryFolderPath);
        File[] queryFolderFiles = queryFolderFile.listFiles((dir, name) -> name.endsWith("easybgp"));
        FXStreamExecutor exec = new FXStreamExecutor();

        JenaSystem.init();
        QC.setFactory(ARQ.getContext(), FacadeX.ExecutorFactory);
        ExecutionContext execCxt = ExecutionContext.create(DatasetGraphFactory.create());

        Assert.assertNotNull(queryFolderFiles);
        for (File bgpFile : queryFolderFiles) {
            String locationFile = bgpFile.getAbsolutePath().substring(0, bgpFile.getAbsolutePath().length() - 11) + "location.txt";
            String location = IOUtils.toString(new File(locationFile).toURI(), Charset.defaultCharset());
            location = location.replace("..", resultFolder);
            BasicPattern bp = BGPTestUtils.readBGP(bgpFile.toURI().toURL());
            properties.setProperty(IRIArgument.LOCATION.toString(), location);
            OpBGP op = new OpBGP(bp);
            OpService opService = new OpService(NodeFactory.createURI("x-sparql-anything:location=" + location), op, false);
            QueryIterator qi;
            int queryMarker = bgpFile.getName().indexOf("-");
            String query = bgpFile.getName().substring(0, queryMarker);
            int startBGPNumber = bgpFile.getName().indexOf("-", bgpFile.getName().length() - 17);
            int endBGPNumber = bgpFile.getName().indexOf("-", startBGPNumber + 1);
            String bgpNumber = bgpFile.getName().substring(startBGPNumber + 1, endBGPNumber);
            try {

                long t0 = System.currentTimeMillis();
                qi = exec.exec(op, properties);
                printResults(qi);
                long t1 = System.currentTimeMillis();

                long t2 = System.currentTimeMillis();
                qi = QC.execute(opService, OpExecutor.createRootQueryIterator(execCxt), execCxt);
                printResults(qi);
                long t3 = System.currentTimeMillis();

                System.out.printf("%s-%s\t%d\t%s\t%s\t%d\t%d%n", query, bgpNumber, bp.size(), FilenameUtils.getName(location), size, (t1 - t0), (t3 - t2));
            } catch (NotATreeException e) {
                System.out.printf("%s-%s not a tree %s\n", bgpNumber, query, bgpFile.getAbsolutePath());
            }


        }
    }

    public void printResults(QueryIterator qi) {
        while (qi.hasNext()) {
            qi.next();
        }
    }

}
