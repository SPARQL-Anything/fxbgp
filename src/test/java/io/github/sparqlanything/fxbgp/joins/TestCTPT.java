package io.github.sparqlanything.fxbgp.joins;

import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceContainerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceTypeImpl;
import io.github.sparqlanything.model.IRIArgument;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class TestCTPT {

    @Rule
    public TestName name = new TestName();

    /*


        case 3:


        case 4:
        (_, ?p1:TP, t1:T) (_, ?p2:TP, t2:T) [t1, t2] ::: ?p1 = <rdf:type, ?p2 = rdf:type>

        case 5:
        (_, ?p1:TP, ?t1:T) (_, ?p2:TP, ?t2:T) [t1, t2] :::
            <?p1 = rdf:type, ?p2 = rdf:type, ?t1 = t1, ?t2 = ?t1>
            <?p1 = rdf:type, ?p2 = rdf:type, ?t1 = t2, ?t2 = ?t2>
            <?p1 = rdf:type, ?p2 = rdf:type, ?t1 = t1, ?t2 = ?t2>
            <?p1 = rdf:type, ?p2 = rdf:type, ?t1 = t2, ?t2 = ?t1>


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

    // (_, a:TP, A:T) [A] :::

    @Test
    public void test4() throws IOException, URISyntaxException {
        TestUtils.assertEquals(name.getMethodName(), pattern);
    }

    // (_, ?p:TP, t:T) [t] ::: ?p = rdf:type

    @Test
    public void test5() throws IOException, URISyntaxException {
        TestUtils.assertEquals(name.getMethodName(), pattern);
    }

    // (_, a:TP, t1:T) [t] ::: X

    @Test
    public void test4_test4n_test4n() throws IOException, URISyntaxException {
        TestUtils.assertEquals(name.getMethodName(), pattern, false);
    }


    // (_, ?p:TP, A1:T) (_, ?p:TP, A2:T) [A1, A2] ::: ?p = rdf:type

    @Test
    public void sc_doubleType_doubleType() throws IOException, URISyntaxException {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.ROOT.toString(), "https://example.org/root");
        DataSourceContainer dataSourceContainer = new DataSourceContainerImpl("A", properties);
        dataSourceContainer.addType(new DataSourceTypeImpl("A1", properties));
        dataSourceContainer.addType(new DataSourceTypeImpl("A2", properties));

        TestUtils.assertEquals(name.getMethodName(), pattern, true, Set.of(dataSourceContainer), properties);
    }
}
