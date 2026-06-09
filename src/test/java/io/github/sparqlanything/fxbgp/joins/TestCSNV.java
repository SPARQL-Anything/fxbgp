package io.github.sparqlanything.fxbgp.joins;

import io.github.sparqlanything.fxbgp.joins.utils.TestCase;
import io.github.sparqlanything.fxbgp.joins.utils.TestCaseExecutor;
import io.github.sparqlanything.fxbgp.joins.utils.impl.CSVTestCase;
import io.github.sparqlanything.fxbgp.joins.utils.impl.TestCaseExecutorImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class TestCSNV {

    @Rule
    public TestName name = new TestName();

    TestCaseExecutor executor = new TestCaseExecutorImpl();

    @Test
    public void csv_00001() {
        // csv_00001  [(_:b0 rdf:_1 "a")]  [a,b,c]  [("a" -> "a")(_:s -> _:row_1)(rdf:_1 -> rdf:_1)]
        TestCase testCase = new CSVTestCase(name.getMethodName());
        //System.out.println(testCase);
        executor.assertEquals(testCase);
    }

    @Test
    public void csv_00002() {
        // csv_00002  [(_:b0 rdf:_1 ?o)]  [a,b,c]  [(_:s -> _:row_1)(rdf:_1 -> rdf:_1)(?o -> "a")]
        TestCase testCase = new CSVTestCase(name.getMethodName());
        // System.out.println(testCase);
        executor.assertEquals(testCase);
    }

    @Test
    public void csv_00003() {
        // csv_00003  [(_:b0 ?p ?o)]  [a,b,c]  [(?p -> rdf:_3)(_:s -> _:row_1)(?o -> "c")]  [(?p -> rdf:_1)(_:s -> _:row_1)(?o -> "a")]  [(?p -> rdf:_2)(_:s -> _:row_1)(?o -> "b")]
        TestCase testCase = new CSVTestCase(name.getMethodName());
        //System.out.println(testCase);
        executor.assertEquals(testCase);
    }

    @Test
    public void csv_00004() {
        // csv_00004  [(?s ?p ?o)]  [a,b,c]  [(?p -> rdf:_3)(?s -> _:row_1)(?o -> "c")]  [(?p -> rdf:_1)(?s -> _:row_1)(?o -> "a")]  [(?p -> rdf:_2)(?s -> _:row_1)(?o -> "b")]
        TestCase testCase = new CSVTestCase(name.getMethodName());
        // System.out.println(testCase);
        executor.assertEquals(testCase);
    }

    @Test
    public void csv_00005() {
        // csv_00005  [(?s ?p ?o)]  [a,b,c|d,e,f]  [(?p -> rdf:_1)(?s -> _:row_2)(?o -> "d")]  [(?p -> rdf:_2)(?s -> _:row_2)(?o -> "e")]  [(?p -> rdf:_3)(?s -> _:row_2)(?o -> "f")]  [(?p -> rdf:_3)(?s -> _:row_1)(?o -> "c")]  [(?p -> rdf:_1)(?s -> _:row_1)(?o -> "a")]  [(?p -> rdf:_2)(?s -> _:row_1)(?o -> "b")]
        TestCase testCase = new CSVTestCase(name.getMethodName());
        // System.out.println(testCase);
        executor.assertEquals(testCase);
    }

    @Test
    public void csv_00006() {
        // csv_00006  [(?s ?p ?o)]  [a,b,c|d,e,f|g,h,i|]  [(?p -> rdf:_1)(?s -> _:row_2)(?o -> "d")]  [(?p -> rdf:_2)(?s -> _:row_2)(?o -> "e")]  [(?p -> rdf:_3)(?s -> _:row_2)(?o -> "f")]  [(?p -> rdf:_3)(?s -> _:row_1)(?o -> "c")]  [(?p -> rdf:_1)(?s -> _:row_1)(?o -> "a")]  [(?p -> rdf:_2)(?s -> _:row_1)(?o -> "b")]  [(?p -> rdf:_1)(?s -> _:row_3)(?o -> "g")]  [(?p -> rdf:_2)(?s -> _:row_3)(?o -> "h")]  [(?p -> rdf:_3)(?s -> _:row_3)(?o -> "i")]
        TestCase testCase = new CSVTestCase(name.getMethodName());
        // System.out.println(testCase);
        executor.assertEquals(testCase);
    }

    @Test
    public void csv_00007() {
        // csv_00007  [(?s ?p1 ?o1) (?s ?p2 ?o2)]  [a,b,c]  [(?p1 -> rdf:_1)(?o1 -> "a")(?p2 -> rdf:_3)(?s -> _:row_1)(?o2 -> "c")]  [(?p1 -> rdf:_2)(?o1 -> "b")(?p2 -> rdf:_3)(?s -> _:row_1)(?o2 -> "c")]  [(?p1 -> rdf:_3)(?o1 -> "c")(?p2 -> rdf:_3)(?s -> _:row_1)(?o2 -> "c")]  [(?p1 -> rdf:_1)(?o1 -> "a")(?p2 -> rdf:_1)(?s -> _:row_1)(?o2 -> "a")]  [(?p1 -> rdf:_1)(?o1 -> "a")(?p2 -> rdf:_2)(?s -> _:row_1)(?o2 -> "b")]  [(?p1 -> rdf:_2)(?o1 -> "b")(?p2 -> rdf:_1)(?s -> _:row_1)(?o2 -> "a")]  [(?p1 -> rdf:_2)(?o1 -> "b")(?p2 -> rdf:_2)(?s -> _:row_1)(?o2 -> "b")]  [(?p1 -> rdf:_3)(?o1 -> "c")(?p2 -> rdf:_1)(?s -> _:row_1)(?o2 -> "a")]  [(?p1 -> rdf:_3)(?o1 -> "c")(?p2 -> rdf:_2)(?s -> _:row_1)(?o2 -> "b")]
        TestCase testCase = new CSVTestCase(name.getMethodName());
        // System.out.println(testCase);
        executor.assertEquals(testCase);
    }

    @Test
    public void csv_00008() {
        // csv_00008  [(_:b0 rdf:_2 "a")]  [a,b,c]  No results
        TestCase testCase = new CSVTestCase(name.getMethodName());
        // System.out.println(testCase);
        executor.assertEquals(testCase);
    }

    @Test
    public void csv_00009() {
        // csv_00009  [(_:b0 rdf:_4 ?o)]  [a,b,c]  No results
        TestCase testCase = new CSVTestCase(name.getMethodName());
        // System.out.println(testCase);
        executor.assertEquals(testCase);
    }

    @Test
    public void csv_00010() {
        // csv_00010  [(ex:s ?p ?o)]  [a,b,c]  No results
        TestCase testCase = new CSVTestCase(name.getMethodName());
        // System.out.println(testCase);
        executor.assertEquals(testCase);
    }


}
