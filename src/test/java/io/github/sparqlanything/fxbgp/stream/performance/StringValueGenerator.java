package io.github.sparqlanything.fxbgp.stream.performance;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.*;
import java.util.stream.IntStream;

public class StringValueGenerator {

    private static final RandomStringUtils RANDOM_STRING_UTILS = RandomStringUtils.insecure();

    public static List<List<String>> createRecordTypes(int numberOfRowTypes, int numberOfColumns, int numberOfDifferentValues, int minChars, int maxChars) {
        StringValueGenerator stringValueGenerator = new StringValueGenerator();

        Set<String> values = stringValueGenerator.generateValues(numberOfDifferentValues, minChars, maxChars);

        List<String> orderedValues = new ArrayList<>(values);
        List<List<String>> rowTypes = new ArrayList<>();

        for (int i = 0; i < numberOfRowTypes; i++) {
            Collections.shuffle(orderedValues);
            rowTypes.add(new ArrayList<>(orderedValues.subList(0, numberOfColumns)));
        }
        return rowTypes;
    }

    public Set<String> generateValues(int numberOfValues, int minChars, int maxChars) {
        Set<String> result = new HashSet<>();
        IntStream.range(0, numberOfValues).boxed().forEach(n -> {
            result.add(RANDOM_STRING_UTILS.nextAlphabetic(minChars, maxChars));
        });
        return result;
    }
}
