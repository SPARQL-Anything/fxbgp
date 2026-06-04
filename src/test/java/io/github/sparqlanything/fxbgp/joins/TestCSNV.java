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
        TestCase testCase = new CSVTestCase(name.getMethodName());
        executor.assertEquals(testCase);
    }

    @Test
    public void csv_00002() {
        TestCase testCase = new CSVTestCase(name.getMethodName());
        executor.assertEquals(testCase);
    }

    @Test
    public void csv_00003() {
        TestCase testCase = new CSVTestCase(name.getMethodName());
        executor.assertEquals(testCase);
    }

    @Test
    public void csv_00004() {
        TestCase testCase = new CSVTestCase(name.getMethodName());
        executor.assertEquals(testCase);
    }

    @Test
    public void csv_00005() {
        TestCase testCase = new CSVTestCase(name.getMethodName());
        executor.assertEquals(testCase);
    }

    @Test
    public void csv_00006() {
        TestCase testCase = new CSVTestCase(name.getMethodName());
        executor.assertEquals(testCase);
    }


}
