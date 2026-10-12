package io.github.sparqlanything.fxbgp.joins;

import io.github.sparqlanything.fxbgp.joins.utils.TestCase;
import io.github.sparqlanything.fxbgp.joins.utils.TestCaseExecutor;
import io.github.sparqlanything.fxbgp.joins.utils.impl.CSVTestCase;
import io.github.sparqlanything.fxbgp.joins.utils.impl.TestCaseExecutorImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

public class TestCSNC {

    @Rule
    public TestName name = new TestName();

    TestCaseExecutor executor = new TestCaseExecutorImpl();

    @Test
    public void csv_00011() {
        // csv_00011  [(?s ?p ?o)]  [a,b,c]  [_:,rdf:_1,_:row_1]
        TestCase testCase = new CSVTestCase(name.getMethodName());
        // System.out.println(testCase);
        executor.assertEquals(testCase);
    }

}
