package io.github.sparqlanything.fxbgp.joins;

import io.github.sparqlanything.fxbgp.FX;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestCSNC {
    @Rule
    public TestName name = new TestName();


    static Set<List<FX>> pattern = new HashSet<>();

    static {
        pattern.add(List.of(FX.Container, FX.SlotNumber, FX.Container));
    }

    // (_, a:TP, A:T) [A] :::

    @Test
    public void test4() throws IOException, URISyntaxException {
    }
}
