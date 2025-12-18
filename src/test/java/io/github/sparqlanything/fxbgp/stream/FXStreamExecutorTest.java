package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.BGPTestUtils;
import io.github.sparqlanything.model.IRIArgument;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

public class FXStreamExecutorTest extends BGPTestUtils {
    FXStreamExecutor executor;
    @Rule
    public TestName testName = new TestName();
    private URL input;
    private BasicPattern bp;

    @Before
    public void before(){
        executor = new FXStreamExecutor();
    }

    @Test
    public void test1_csv_all() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(21, it.size());
        show(it.iterator());
    }

    @Test
    public void test1_csv_s1() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(1, it.size());
        show(it.iterator());
    }


    @Test
    public void test1_csv_s2() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(1, it.size());
        show(it.iterator());
    }


    @Test
    public void test1_csv_s3() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(1, it.size());
        show(it.iterator());
    }

    @Test
    public void test1_csv_s4() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(5, it.size());
        show(it.iterator());
    }


    @Test
    public void test1_csv_s5() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(1, it.size());
        show(it.iterator());
    }

    @Test
    public void test1_csv_s6() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(1, it.size());
        show(it.iterator());
    }

    @Ignore
    @Test
    public void test1_csv_s7() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        //Assert.assertEquals(1, it.size());
        show(it.iterator());
    }

    private void show(Iterator<QuerySolution> qit){
        while(qit.hasNext()){
            L.info(" ---- ");
            QuerySolution qs = qit.next();
            Iterator<String> it = qs.varNames();
            while(it.hasNext()){
                String var = it.next();
                L.info("Solution: {} -> {}", var, qs.get(var));
            }
        }
    }


    @Test
    public void test1_csv_strict() throws IOException, NotATreeException {
        String name = testName.getMethodName();
        prepare(name);
        Iterator<QuerySolution> it = executor.exec(new OpBGP(bp), properties());
        show(it);
    }

    private Properties properties() {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.LOCATION.toString(), input.toString());
        return properties;
    }

    private void prepare(String methodName) throws IOException {
        String[] spl = testName.getMethodName().split("_");
        String inputName = spl[0] + "." + spl[1];
        String easyBGPName = spl[2];
        this.input = getClass().getClassLoader().getResource("./stream/" + inputName);

        this.bp = readBGP("./stream/" + easyBGPName);
    }

    public Set<QuerySolution> set(Iterator<QuerySolution> qit){
        Set<QuerySolution> set = new HashSet<>();
        while(qit.hasNext()){
            set.add(qit.next());
        }
        return set;
    }

    @Test
    public void matchesTest(){
        boolean isFalse = NodeFactory.createVariable("v").matches(
                NodeFactory.createURI("http://www.example.org/"));
        System.out.println (isFalse);
//        PatternMatchData.execute()
        Node pnode = Var.alloc("a");
        Node dnode = NodeFactory.createURI("http://example.org/uri1");
        L.info("Pattern node: {}", pnode);
        L.info("Incoming node: {}", dnode);
        L.info("Pattern matches Incoming: {}", Matching.nodeMatches(dnode, pnode));
        L.info("Incoming matches Pattern: {}", Matching.nodeMatches(pnode, dnode));
    }
}
