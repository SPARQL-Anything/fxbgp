package io.github.sparqlanything.fxbgp.joins;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.net.URISyntaxException;

public class TestCTPT {

    @Rule
    public TestName name = new TestName();

    @Test
    public void test4() throws IOException, URISyntaxException {
        TestUtils.assertEquals(name.getMethodName());
    }
}
