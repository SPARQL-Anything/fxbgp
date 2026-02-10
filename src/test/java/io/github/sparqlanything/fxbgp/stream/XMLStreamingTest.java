package io.github.sparqlanything.fxbgp.stream;

import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

public class XMLStreamingTest extends FXStreamExecutorTest{

    @Test
    public void test1_xml_all_basic() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(getOpBGP(), properties(false)));
        //Assert.assertTrue(it.size() == 9);
        show(getGraphFrom_abc(it));
    }

}
