package io.github.sparqlanything.fxbgp.stream;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MatchingTest {


    @Test
    public void testSubsets(){
        List<List> s = Arrays.asList(List.of("a"), List.of("b", "b"), List.of("c"));
        Assert.assertEquals(8, FXTreeUtils.subsets(s).size());
    }
}
