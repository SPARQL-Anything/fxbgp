package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.BGPTestUtils;
import io.github.sparqlanything.model.IRIArgument;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.rdf.model.RDFNode;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

public class FXStreamExecutorTest extends BGPTestUtils {
    FXStreamExecutor executor;
    @Rule
    public TestName testName = new TestName();
    private URL input;
    private BasicPattern bp;
    private String flavour;
    @Before
    public void before(){
        executor = new FXStreamExecutor();
    }

    @Test
    public void test1_csv_all_basic() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        show(it.iterator());
        Assert.assertEquals(21, it.size());
        Assert.assertTrue(
            rem(it,
                new String[]{"a", "test1.csv\\#$", "b", "\\#type$", "c", "/[rR]oot$"},
                    new String[]{"a", "test1.csv\\#$", "b", "\\#_1$", "c", "test1.csv\\#row1$"},
                    new String[]{"a", "test1.csv\\#$", "b", "\\#_2$", "c", "test1.csv\\#row2$"},
                    new String[]{"a", "test1.csv\\#$", "b", "\\#_3$", "c", "test1.csv\\#row3$"},
                    new String[]{"a", "test1.csv\\#$", "b", "\\#_4$", "c", "test1.csv\\#row4$"},
                    //
                    new String[]{"a", "test1.csv\\#row1$", "b", "\\#_1$", "c", "^H1$"},
                    new String[]{"a", "test1.csv\\#row1$", "b", "\\#_2$", "c", "^H2$"},
                    new String[]{"a", "test1.csv\\#row1$", "b", "\\#_3$", "c", "^H3$"},
                    new String[]{"a", "test1.csv\\#row1$", "b", "\\#_4$", "c", "^H4$"},
                    //
                    new String[]{"a", "test1.csv\\#row2$", "b", "\\#_1$", "c", "^A1$"},
                    new String[]{"a", "test1.csv\\#row2$", "b", "\\#_2$", "c", "^A2$"},
                    new String[]{"a", "test1.csv\\#row2$", "b", "\\#_3$", "c", "^A3$"},
                    new String[]{"a", "test1.csv\\#row2$", "b", "\\#_4$", "c", "^A4$"},
                    //
                    new String[]{"a", "test1.csv\\#row3$", "b", "\\#_1$", "c", "^B1$"},
                    new String[]{"a", "test1.csv\\#row3$", "b", "\\#_2$", "c", "^B2$"},
                    new String[]{"a", "test1.csv\\#row3$", "b", "\\#_3$", "c", "^B3$"},
                    new String[]{"a", "test1.csv\\#row3$", "b", "\\#_4$", "c", "^B4$"},
                    //
                    new String[]{"a", "test1.csv\\#row4$", "b", "\\#_1$", "c", "^C1$"},
                    new String[]{"a", "test1.csv\\#row4$", "b", "\\#_2$", "c", "^C2$"},
                    new String[]{"a", "test1.csv\\#row4$", "b", "\\#_3$", "c", "^C3$"},
                    new String[]{"a", "test1.csv\\#row4$", "b", "\\#_4$", "c", "^C4$"}
            ));
    }

    @Test
    public void test1_csv_all_headers() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        show(it.iterator());
        Assert.assertEquals(16, it.size());
        Assert.assertTrue(
                rem(it,
                        new String[]{"a", "test1.csv\\#$", "b", "\\#type$", "c", "/[rR]oot$"},
                        new String[]{"a", "test1.csv\\#$", "b", "\\#_1$", "c", "test1.csv\\#row1$"},
                        new String[]{"a", "test1.csv\\#$", "b", "\\#_2$", "c", "test1.csv\\#row2$"},
                        new String[]{"a", "test1.csv\\#$", "b", "\\#_3$", "c", "test1.csv\\#row3$"},
                        //new String[]{"a", "test1.csv\\#$", "b", "\\#_4$", "c", "test1.csv\\#row4$"},
                        //
                        new String[]{"a", "test1.csv\\#row1$", "b", "/H1$", "c", "^A1$"},
                        new String[]{"a", "test1.csv\\#row1$", "b", "/H2$", "c", "^A2$"},
                        new String[]{"a", "test1.csv\\#row1$", "b", "/H3$", "c", "^A3$"},
                        new String[]{"a", "test1.csv\\#row1$", "b", "/H4$", "c", "^A4$"},
                        //
                        new String[]{"a", "test1.csv\\#row2$", "b", "/H1$", "c", "^B1$"},
                        new String[]{"a", "test1.csv\\#row2$", "b", "/H2$", "c", "^B2$"},
                        new String[]{"a", "test1.csv\\#row2$", "b", "/H3$", "c", "^B3$"},
                        new String[]{"a", "test1.csv\\#row2$", "b", "/H4$", "c", "^B4$"},
                        //
                        new String[]{"a", "test1.csv\\#row3$", "b", "/H1$", "c", "^C1$"},
                        new String[]{"a", "test1.csv\\#row3$", "b", "/H2$", "c", "^C2$"},
                        new String[]{"a", "test1.csv\\#row3$", "b", "/H3$", "c", "^C3$"},
                        new String[]{"a", "test1.csv\\#row3$", "b", "/H4$", "c", "^C4$"}
                        //
                ));
    }

    @Test
    public void test1_csv_s1() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(1, it.size());
        Assert.assertTrue(it.iterator().next().getResource("a").getURI().endsWith("test1.csv#row2"));
        show(it.iterator());
    }


    @Test
    public void test1_csv_s2() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(1, it.size());
        Assert.assertTrue(it.iterator().next().getResource("a").getURI().endsWith("test1.csv#row2"));
        show(it.iterator());
    }

    @Test
    public void test1_csv_s3() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(1, it.size());
        Assert.assertTrue(it.iterator().next().getResource("a").getURI().endsWith("/test1.csv#row4"));
        Assert.assertTrue(it.iterator().next().getResource("r").getURI().endsWith("/test1.csv#"));
        show(it.iterator());
    }

    @Test
    public void test1_csv_s4() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(5, it.size());
        show(it.iterator());
        Assert.assertTrue(
                rem(it,
                        new String[]{"a", "test1.csv\\#row3$", "b", "^B1$"},
                        new String[]{"a", "test1.csv\\#$", "b", "test1.csv\\#row1$"},
                        new String[]{"a", "test1.csv\\#row1$", "b", "^H1$"},
                        new String[]{"a", "test1.csv\\#row2$", "b", "^A1$"}, // ok
                        new String[]{"a", "test1.csv\\#row4$", "b", "^C1$"} // ok
                ));

    }

    @Test
    public void test1_csv_s5() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(1, it.size());
        Assert.assertTrue(
                rem(it,
                        new String[]{"a", "test1.csv\\#row1$", "b", "\\#_1$"}
                ));
        show(it.iterator());
    }

    @Test
    public void test1_csv_s6() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(1, it.size());
        Assert.assertTrue(
                rem(it,
                        new String[]{"a", "test1.csv\\#$", "b", "\\#_1$", "c", "test1.csv\\#row1$", "d", "\\#_1$"}
                ));
        show(it.iterator());
    }

    //@Ignore
    @Test
    public void test1_csv_s7() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(1, it.size());
        show(it.iterator());
    }

    @Test
    public void test1_csv_s8() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<QuerySolution> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(1, it.size());
        show(it.iterator());
    }

    @Test
    public void squash(){
        String fx = "http://sparql.xyz/facade-x/ns/" ;
        String xyz = "http://sparql.xyz/facade-x/data/";
        String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        String rdfs = "http://www.w3.org/2000/01/rdf-schema#";

        String n = rdf + "_1";
        System.err.println(n.toString().substring(n.lastIndexOf('#')));
         n = fx + "root";
        System.err.println(n.toString().substring(n.lastIndexOf('/')));
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
    public static <T> List<List<T>> subsets(List<T> in) {
        List<List<T>> out = new ArrayList<>();
        out.add(new ArrayList<>());
        for (T elem : in) {
            int size = out.size();
            for (int i = 0; i < size; i++) {
                List<T> newSubset = new ArrayList<>(out.get(i));
                newSubset.add(elem);
                out.add(newSubset);
            }
        }
        return out;
    }

    @Test
    public void testSubsets(){
        List<List> s = Arrays.asList(List.of("a"), List.of("b", "b"), List.of("c"));
        System.out.println(subsets(s));
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
        if("headers".equals(flavour)){
            properties.setProperty(CSVTriplifier2.PROPERTY_HEADERS.toString(), "true");
        }
        return properties;
    }

    @Test
    public void testMatchingEquals(){
        List<Node> n1 = new ArrayList<>();
        List<Node> n2 = new ArrayList<>();
        Node a = NodeFactory.createURI("http://example.org/a");
        Node b = NodeFactory.createURI("http://example.org/a");
        Node l1a = NodeFactory.createLiteral("1", XSDDatatype.XSDinteger);
        Node l1b = NodeFactory.createLiteral("1", XSDDatatype.XSDinteger);
        n1.add(a);
        n1.add(l1a);
        n2.add(b);
        n2.add(l1b);
        Assert.assertEquals(n1, n2);

        Map<String,String> map1 = new HashMap<>();
        Map<String,String> map2 = new HashMap<>();
        map1.put("a", "1");
        map2.put("a", "1");
        map1.put("b", "1");
        map2.put("b", "1");
        Assert.assertEquals(map1, map2);
    }

    private void prepare(String methodName) throws IOException {
        String[] spl = testName.getMethodName().split("_");
        String inputName = spl[0] + "." + spl[1];
        String easyBGPName = spl[2];
        if(spl.length == 4){
            this.flavour = spl[3];
        }
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

    public boolean rem(Set<QuerySolution> qs, String[] ... var_regex_conditions){
        Set<QuerySolution> set = new HashSet<>(qs);
        Set<String[]> unmet = new HashSet<>();
        Set<QuerySolution> foundd = new HashSet<>();
        for(String[] var_regex : var_regex_conditions){
            boolean found = false;
            // If a query solution resolves all conditions is removed
            for(QuerySolution q : qs){
                int successes = 0;
                for(int index = 0; index < var_regex.length; index = index + 2){
                    String var = var_regex[index];
                    String regex = var_regex[index + 1];
                    RDFNode node = q.get(var);
                    String vvv = node.toString();
                    //L.info("Testing {} vs {}", node, regex);
                    if(Pattern.compile(regex).matcher(vvv).find()){
                        //L.info("Success {} vs {}", node, regex);
                        successes ++;
                    }else {
                        break;
                    }
                }
                if(successes == var_regex.length/2){
                    //L.info("Successes {} vs {}", successes, var_regex.length/2);
                    set.remove(q);
                    found = true;
                    foundd.add(q);
                }
            }
            //
            if(!found){
                unmet.add(var_regex);
            }
        }
        L.info("Solutions matching: {}", foundd.size());
        if(!set.isEmpty()){
            L.error("Solutions not matching anything: {}", set.size());
            L.error("Example:");
            for (QuerySolution q : set) {
                L.error(q.toString());
                break;
            }
        }
        if(!unmet.isEmpty()){
            L.error("Matches without solutions: {}", unmet.size());
            L.error("Example:");
            for (String[] q : unmet) {
                L.error(StringUtils.join(q, ","));
                break;
            }
        }
        return set.isEmpty() && unmet.isEmpty();
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
