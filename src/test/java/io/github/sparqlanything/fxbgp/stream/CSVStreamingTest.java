package io.github.sparqlanything.fxbgp.stream;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.algebra.op.OpGraph;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

public class CSVStreamingTest extends FXStreamExecutorTest{

    @Test
    public void test1_csv_all_basic() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(getOpBGP(), properties(false)));
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
        Set<Binding> it = set(executor.exec(getOpBGP(), properties(false)));
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
        OpGraph opGraph = new OpGraph(NodeFactory.createVariable("g"), getOpBGP());
        Set<Binding> it = set(executor.exec(opGraph, properties()));
        show(it.iterator());
        Assert.assertEquals(21, it.size());
    }

    @Test
    public void test1_csv_s1() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(getOpBGP(), properties(false)));
        Assert.assertEquals(1, it.size());
        Assert.assertTrue(it.iterator().next().get("a").getURI().endsWith("test1.csv#row2"));
        show(it.iterator());
    }


    @Test
    public void test1_csv_s2() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(getOpBGP(), properties(false)));
        Assert.assertEquals(1, it.size());
        Assert.assertTrue(it.iterator().next().get("a").getURI().endsWith("test1.csv#row2"));
        show(it.iterator());
    }

    @Test
    public void test1_csv_s3() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(getOpBGP(), properties(false)));
        Assert.assertEquals(1, it.size());
        Assert.assertTrue(it.iterator().next().get("a").getURI().endsWith("/test1.csv#row4"));
        Assert.assertTrue(it.iterator().next().get("r").getURI().endsWith("/test1.csv#"));
        show(it.iterator());
    }

    @Test
    public void test1_csv_s4() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(getOpBGP(), properties(false)));
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
        Set<Binding> it = set(executor.exec(getOpBGP(), properties(false)));
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
        Set<Binding> it = set(executor.exec(getOpBGP(), properties(false)));
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
        Set<Binding> it = set(executor.exec(getOpBGP(), properties()));
        Assert.assertEquals(1, it.size());
        show(it.iterator());
    }

    @Test
    public void test1_csv_s8() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(getOpBGP(), properties(false)));
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
        Set<Binding> it = set(executor.exec(getOpBGP(), properties(false)));
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
        Set<Binding> it = set(executor.exec(getOpBGP(), properties(false)));
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
        Set<Binding> it = set(executor.exec(getOpBGP(), p));
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
        Set<Binding> it = set(executor.exec(getOpBGP(), properties()));
        Assert.assertEquals(21, it.size());
        show(it.iterator());
        for(Binding w: it){
            Assert.assertTrue(w.get("b1").equals(w.get("b2")));
        }
    }

    @Test
    public void test1_csv_s13() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(getOpBGP(), properties()));
        Assert.assertEquals(1, it.size());
        show(it.iterator());
        for(Binding w: it){
            Assert.assertTrue(w.get("b1").equals(w.get("b2")));
        }
    }


    @Test
    public void test1_csv_n1() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(getOpBGP(), properties()));
        Assert.assertEquals(0, it.size());
    }

    @Test
    public void test2_csv_all() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(getOpBGP(), properties()));
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
        Set<Binding> it = set(executor.exec(getOpBGP(), properties()));
        Assert.assertEquals(0, it.size());
    }

    @Test
    public void test2_csv_n3() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Exception q = null;
        try {
            set(executor.exec(getOpBGP(), properties())).size();
        }catch(NotATreeException e){
            q =e;
        }
        Assert.assertNotNull(q);
    }

    @Test
    public void test2_csv_n4() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(getOpBGP(), properties()));
        Assert.assertEquals(0, it.size());
    }

    @Test
    public void test2_csv_n1() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        Set<Binding> it = set(executor.exec(getOpBGP(), properties()));
        Assert.assertEquals(0, it.size());
    }


    @Test
    public void test1_csv_strict() throws IOException, NotATreeException {
        String name = testName.getMethodName();
        prepare(name);
        QueryIterator it = executor.exec(getOpBGP(), properties());
        show(it);
    }
}
