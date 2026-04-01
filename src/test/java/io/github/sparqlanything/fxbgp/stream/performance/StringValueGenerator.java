package io.github.sparqlanything.fxbgp.stream.performance;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

public class StringValueGenerator {

    private static final RandomStringUtils RANDOM_STRING_UTILS = RandomStringUtils.insecure();

    public Set<String> generateValues(int numberOfValues, int minChars, int maxChars) {
        Set<String> result = new HashSet<>();
        IntStream.range(0, numberOfValues).boxed().forEach(n -> {
            result.add(RANDOM_STRING_UTILS.nextAlphabetic(minChars, maxChars));
        });
        return result;
    }
}
