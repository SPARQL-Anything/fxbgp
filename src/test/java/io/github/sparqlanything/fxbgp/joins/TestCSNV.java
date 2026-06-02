package io.github.sparqlanything.fxbgp.joins;

import io.github.sparqlanything.fxbgp.FX;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.model.TestClass;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestCSNV {

    @Rule
    public TestName name = new TestName();

    static Set<List<FX>> pattern = new HashSet<>();

    static {
        pattern.add(List.of(FX.Container, FX.SlotNumber, FX.Value));
    }

    // Test name pattern <filename input>_<filename query>_<filename expected result>_<format>


    // (_, rdf:_1, "a") [("a", "b", "c")] ::: MATCH
    @Test
    public void input1_firstSlot_firstSlot_csv() throws IOException, URISyntaxException {
        TestUtils.assertEquals(name.getMethodName(), pattern, true);
    }
}
