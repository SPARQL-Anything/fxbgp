package io.github.sparqlanything.fxbgp;

import io.github.sparqlanything.engine.Utils;
import io.github.sparqlanything.fxbgp.stream.FXStreamExecutor;
import io.github.sparqlanything.fxbgp.stream.NotATreeException;
import io.github.sparqlanything.fxbgp.stream.SharedPathAccessor;
import io.github.sparqlanything.model.IRIArgument;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static io.github.sparqlanything.fxbgp.BGPTestUtils.readBGP;

public class IntegrationTest {

    @Test
    public void testCMP2Number(){
        JenaSystem.init();
        int number = 10;
        Node c1 = RDF.li(number).asNode();
        Assert.assertTrue(SharedPathAccessor.toNumber(c1) == number);
    }

    @Test
    public void issue154() throws IOException, NotATreeException {

        String input = String.valueOf(getClass().getClassLoader().getResource("./integration-test/issue154.xml"));
        BasicPattern bp = readBGP("./integration-test/issue154");
        FXStreamExecutor executor = new FXStreamExecutor();
        Properties p = new Properties();
        p.setProperty(IRIArgument.LOCATION.toString(), input);
        p.setProperty(IRIArgument.MEDIA_TYPE.toString(), "application/xml");
        QueryIterator qi = executor.exec(new OpBGP(bp), p);
        while(qi.hasNext()){
            System.out.println("next");
            System.out.println(Utils.bindingToString(qi.next()));
        }
    }
}
