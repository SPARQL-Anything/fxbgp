package io.github.sparqlanything.fxbgp.joins;

import io.github.sparqlanything.fxbgp.FX;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestCTPT {

    @Rule
    public TestName name = new TestName();

    /*

        case 1:
        (_, a:TP, A:T) [A] :::

        case 2:
        (_, ?p:TP, t:T) [t] ::: ?p = rdf:type

        case 3:
        (_, ?p:TP, t1:T) (_, ?p:TP, t2:T) [t1, t2] ::: ?p = rdf:type

        case 4:
        (_, ?p1:TP, t1:T) (_, ?p2:TP, t2:T) [t1, t2] ::: ?p1 = <rdf:type, ?p2 = rdf:type>

        case 5:
        (_, ?p1:TP, ?t1:T) (_, ?p2:TP, ?t2:T) [t1, t2] :::
            <?p1 = rdf:type, ?p2 = rdf:type, ?t1 = t1, ?t2 = ?t1>
            <?p1 = rdf:type, ?p2 = rdf:type, ?t1 = t2, ?t2 = ?t2>
            <?p1 = rdf:type, ?p2 = rdf:type, ?t1 = t1, ?t2 = ?t2>
            <?p1 = rdf:type, ?p2 = rdf:type, ?t1 = t2, ?t2 = ?t1>

        case N1:
        (_, a:TP, t1:T) [t] ::: X

        case N2:
        (_, a:TP, t1:T) (_, a:TP, t2:T) [t] ::: X

        case N3:
        (_, a:TP, t1:T) (_, a:TP, t2:T) [t1] ::: X

        case N4:
        (_, a:TP, t1:T) (_, a:TP, t2:T) [t1, t] ::: X

         */

    static Set<List<FX>> pattern = new HashSet<>();

    static {
        pattern.add(List.of(FX.Container, FX.TypeProperty, FX.Type));
    }

    @Test
    public void test4() throws IOException, URISyntaxException {
        TestUtils.assertEquals(name.getMethodName(), pattern);
    }

    @Test
    public void test5() throws IOException, URISyntaxException {
        TestUtils.assertEquals(name.getMethodName(), pattern);
    }
}
