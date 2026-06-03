package io.github.sparqlanything.fxbgp.joins;

import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.fxbgp.joins.utils.TestCase;
import io.github.sparqlanything.fxbgp.joins.utils.TestCaseExecutor;
import io.github.sparqlanything.fxbgp.joins.utils.impl.CSVTestCase;
import io.github.sparqlanything.fxbgp.joins.utils.impl.TestCaseExecutorImpl;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestCSNV {

    @Rule
    public TestName name = new TestName();

    TestCaseExecutor executor = new TestCaseExecutorImpl();

    static Set<List<FX>> pattern = new HashSet<>();

    static {
        pattern.add(List.of(FX.Container, FX.SlotNumber, FX.Value));
    }

    // (_, rdf:_1, ?v) [("a", "b", "c")] ::: MATCH
    @Test
    public void csv_0001() throws IOException, URISyntaxException {
        TestCase testCase = new CSVTestCase(name.getMethodName(), pattern);
        executor.assertEquals(testCase);
    }


}
