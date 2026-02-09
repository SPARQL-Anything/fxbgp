package io.github.sparqlanything.fxbgp.stream;


import com.google.gson.internal.bind.JsonTreeReader;
import io.github.sparqlanything.fxbgp.BGPTestUtils;
import io.github.sparqlanything.json.JSONTriplifier;
import io.github.sparqlanything.model.IRIArgument;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.resultset.ResultSetWriterRegistry;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

public class FXStreamExecutorTest extends BGPTestUtils {
    FXStreamExecutor executor;
    //FXStreamExecutor2 executor2;
    @Rule
    public TestName testName = new TestName();
    private URL input;
    private BasicPattern bp;
    private String flavour;
    private String mediaType;
    @Before
    public void before(){
        executor = new FXStreamExecutor();
    }


    @Test
    public void test1_xml_all_basic() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties(false)));
        show(it.iterator());
    }

    @Test
    public void test1_csv_all_basic() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties(false)));
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
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties(false)));
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
    public void test1_csv_all_quad() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        OpGraph opGraph = new OpGraph(NodeFactory.createVariable("g"), new OpBGP(bp));
        Set<Binding> it = set(executor.exec(opGraph, properties()));
        show(it.iterator());
        Assert.assertEquals(21, it.size());
    }

    @Test
    public void test1_csv_s1() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties(false)));
        Assert.assertEquals(1, it.size());
        Assert.assertTrue(it.iterator().next().get("a").getURI().endsWith("test1.csv#row2"));
        show(it.iterator());
    }


    @Test
    public void test1_csv_s2() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties(false)));
        Assert.assertEquals(1, it.size());
        Assert.assertTrue(it.iterator().next().get("a").getURI().endsWith("test1.csv#row2"));
        show(it.iterator());
    }

    @Test
    public void test1_csv_s3() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties(false)));
        Assert.assertEquals(1, it.size());
        Assert.assertTrue(it.iterator().next().get("a").getURI().endsWith("/test1.csv#row4"));
        Assert.assertTrue(it.iterator().next().get("r").getURI().endsWith("/test1.csv#"));
        show(it.iterator());
    }

    @Test
    public void test1_csv_s4() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties(false)));
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
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties(false)));
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
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties(false)));
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
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(1, it.size());
        show(it.iterator());
    }

    @Test
    public void test1_csv_s8() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties(false)));
        Assert.assertEquals(1, it.size());
        show(it.iterator());
        Assert.assertTrue(
                rem(it,
                        new String[]{"a", "test1.csv\\#row1$", "b", "\\#_2$"}
                ));

    }

    @Test
    public void test1_csv_s9() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties(false)));
        Assert.assertEquals(1, it.size());
        show(it.iterator());
        Assert.assertTrue(
                rem(it,
                        new String[]{"a", "test1.csv\\#row1$", "b", "\\#_2$", "c", "\\#_2$", "d", "\\#_2$", "e", "\\#_2$"}
                ));

    }


    @Test
    public void test1_csv_s10_headers() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties(false)));
        Assert.assertEquals(1, it.size());
        show(it.iterator());
        Assert.assertTrue(
                rem(it,
                        new String[]{"a", "test1.csv\\#row1$", "b", "H1$", "c", "H2$", "d", "H3$", "e", "H4$"}
                ));
    }


    @Test
    public void test1_csv_s11() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Properties p = properties(false);
        p.setProperty("blank-nodes", "false");
        Set<Binding> it = set(executor.exec(new OpBGP(bp), p));
        Assert.assertEquals(1, it.size());
        show(it.iterator());
        Assert.assertTrue(
                rem(it,
                        new String[]{"r", "test1.csv\\#$", "j", "\\#_1$", "a", "test1.csv\\#row1$", "b", "\\#_1$", "c", "\\#_2$", "d", "\\#_3$", "e", "\\#_4$"}
                ));
    }

    @Test
    public void test1_csv_s12() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(21, it.size());
        show(it.iterator());
        for(Binding w: it){
            Assert.assertTrue(w.get("b1").equals(w.get("b2")));
        }
    }

    @Test
    public void test1_csv_s13() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(1, it.size());
        show(it.iterator());
        for(Binding w: it){
            Assert.assertTrue(w.get("b1").equals(w.get("b2")));
        }
    }
    @Test
    public void writers(){
        ResultSetWriterRegistry.init();
        for(Lang l :ResultSetWriterRegistry.registered()){
            L.info("registered: {}", l);
        }
    }

    @Test
    public void test1_csv_n1() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(0, it.size());
    }

    @Test
    public void test2_csv_all() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(21, it.size());
        show(it.iterator());
        for(Binding w: it){
            Assert.assertFalse(w.get("a").equals(w.get("b")));
            Assert.assertFalse(w.get("b").equals(w.get("c")));
        }
    }


    @Test
    public void test2_csv_n2() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(0, it.size());
    }

    @Test
    public void test2_csv_n3() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Exception q = null;
        try {
            set(executor.exec(new OpBGP(bp), properties())).size();
        }catch(NotATreeException e){
            q =e;
        }
        Assert.assertNotNull(q);
    }

    @Test
    public void test2_csv_n4() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(0, it.size());
    }

    @Test
    public void test2_csv_n1() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(new OpBGP(bp), properties()));
        Assert.assertEquals(0, it.size());
    }

    private void show(Iterator<Binding> qit){
        while(qit.hasNext()){
            L.info(" ---- ");
            Binding qs = qit.next();
            Iterator<Var> it = qs.vars();
            while(it.hasNext()){
                String var = it.next().getVarName();
                L.info("Solution: {} -> {}", var, qs.get(var));
            }
        }
    }

    @Test
    public void test1_csv_strict() throws IOException, NotATreeException {
        String name = testName.getMethodName();
        prepare(name);
        QueryIterator it = executor.exec(new OpBGP(bp), properties());
        show(it);
    }

    @Test
    public void test1_json_all() throws IOException, NotATreeException {
        String name = testName.getMethodName();
        prepare(name);
        Set<Binding> it = set(executor.exec(new OpBGP(bp), propertiesNullValueTrue()));
        show(it.iterator());
        Assert.assertEquals(8,it.size());

    }

    @Test
    public void test2_json_a1() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        QueryIterator it = executor.exec(new OpBGP(bp), properties());
        Assert.assertEquals(6,set(it).size());
        show(it);
    }
    @Test
    public void test2_json_a2() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        QueryIterator it = executor.exec(new OpBGP(bp), properties());
        Assert.assertEquals(5,set(it).size());
        show(it);
    }

    private Properties properties(boolean blankNodes){
        return properties(blankNodes, false);
    }
    private Properties properties() {
        return properties(true, false);
    }
    private Properties propertiesNullValueTrue() {
        return properties(true, true);
    }

    private Properties properties(boolean blankNodes, boolean nullValues) {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.LOCATION.toString(), input.toString());
        if("headers".equals(flavour)){
            properties.setProperty(CSVTriplifier2.PROPERTY_HEADERS.toString(), "true");
        }

        properties.setProperty("blank-nodes", blankNodes ? "true" : "false");
        properties.setProperty(JSONTriplifier.PROPERTY_JSONINCLUDENULLVALUES.toString(), nullValues ? "true" : "false");

        String mediaType = null;
        if(input.getPath().endsWith(".csv")){
            mediaType = "text/csv";
        }else if(input.getPath().endsWith(".json")){
            mediaType = "application/json";
        }else if(input.getPath().endsWith(".xml")){
            mediaType = "application/xml";
        }
        properties.setProperty("media-type", mediaType);
        return properties;
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

    public Set<Binding> set(QueryIterator qit){
        Set<Binding> set = new HashSet<>();
        while(qit.hasNext()){
            set.add(qit.next());
        }
        return set;
    }

    public boolean rem(Set<Binding> qs, String[] ... var_regex_conditions){
        Set<Binding> set = new HashSet<>(qs);
        Set<String[]> unmet = new HashSet<>();
        Set<Binding> foundd = new HashSet<>();
        for(String[] var_regex : var_regex_conditions){
            boolean found = false;
            // If a query solution resolves all conditions is removed
            for(Binding q : qs){
                int successes = 0;
                for(int index = 0; index < var_regex.length; index = index + 2){
                    String var = var_regex[index];
                    String regex = var_regex[index + 1];
                    Node node = q.get(var);
                    String vvv ;
                    if(node.isURI()){
                        vvv = node.getURI().toString();
                    }else if(node.isLiteral()){
                        vvv = node.getLiteralLexicalForm().toString();
                    }else if(node.isBlank()) {
                        vvv = node.getBlankNodeLabel().toString();
                    }else{
                        throw new RuntimeException("This should not happen");
                    }
                    //L.trace("Testing {} vs {}", node, regex);
                    if(Pattern.compile(regex).matcher(vvv).find()){
                        //L.trace("Success {} vs {}", node, regex);
                        successes ++;
                    }else {
                        break;
                    }
                }
                if(successes == var_regex.length/2){
                    //L.trace("Successes {} vs {}", successes, var_regex.length/2);
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
        L.trace("Solutions matching: {}", foundd.size());
        if(!set.isEmpty()){
            L.error("Solutions not matching anything: {}", set.size());
            L.error("Example:");
            for (Binding q : set) {
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
        //System.out.println (isFalse);
//        PatternMatchData.execute()
        Node pnode = Var.alloc("a");
        Node dnode = NodeFactory.createURI("http://example.org/uri1");
        L.trace("Pattern node: {}", pnode);
        L.trace("Incoming node: {}", dnode);
        L.trace("Pattern matches Incoming: {}", Matching.nodeMatches(dnode, pnode));
        L.trace("Incoming matches Pattern: {}", Matching.nodeMatches(pnode, dnode));
    }
}
