package io.github.sparqlanything.fxbgp.experiments;

import io.github.sparqlanything.engine.FacadeX;
import io.github.sparqlanything.model.OpComponentsAnalyser;
import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpService;
import org.apache.jena.sparql.core.BasicPattern;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FXBGPUtils {
    private static final Logger L = LoggerFactory.getLogger(FXBGPUtils.class);
    public static List<OpBGP> extract(String query) {
        List<OpBGP> list = new ArrayList<>();
        OpComponentsAnalyser opComponentsAnalyser = new OpComponentsAnalyser(){
            boolean collect = false;
            @Override
            public void visit(OpService opService) {
                collect = true;
                super.visit(opService);
                collect = false;
            }

            @Override
            public void visit(OpBGP opBGP) {
                if(collect == true){
                    boolean skip = false;
//                    List<Triple> result = new ArrayList<>();
//
                    // Collect only triples without fx:properties
                    BasicPattern basicPattern = new BasicPattern();
                    for(Triple t : opBGP.getPattern().getList()) {
                        // ignore BGPs with magic properties, like anySlots
                        if(t.getPredicate().isURI() &&
                                t.getPredicate().getURI().equals(FacadeX.ANY_SLOT_URI)){
                            // Skip BGP
                            skip = true;
                        }
                        if (!t.getSubject().isURI() || !t.getSubject().getURI().equals("http://sparql.xyz/facade-x/ns/properties")) {
                            basicPattern.add(t);
                        }
                    }
                    if(!skip){
                        list.add(new OpBGP(basicPattern));
                    }
                }
                super.visit(opBGP);
            }
        };
        // Queries may include string templates, we clean them with ad-hoc code...
        query = query.replace("%", "");
        try {
            Query q = QueryFactory.create(query);
            Algebra.compile(q).visit(opComponentsAnalyser);
        }catch (Exception e){
            //L.error(e.getMessage());
        }
        return list;
    }

    public static int tripleSize(OpBGP opBGP){
        return opBGP.getPattern().getList().size();
    }

    public static int varSize(OpBGP opBGP){
        Set<Node> vars = new HashSet<>();
        for(Triple t: opBGP.getPattern()){
            if(!t.isConcrete()){
                vars.add(t.getSubject());
            }
        };
        return vars.size();
    }


    @Test
    public void test() throws IOException {
        String qname = "query-244.rq";
        L.info("{}", qname);
        String query = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("./real-world-queries/" + qname), StandardCharsets.UTF_8);
        L.info("{}", query);
        for(OpBGP bgp : extract(query)){
            L.info("{}", bgp.getPattern());
        }
    }
}
