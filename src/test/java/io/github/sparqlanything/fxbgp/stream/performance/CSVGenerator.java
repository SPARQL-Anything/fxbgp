package io.github.sparqlanything.fxbgp.stream.performance;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CSVGenerator {

    public static void generateCSV(int numberOfRows, List<List<String>> rowTypes, String filename) throws IOException {
        FileWriter fw = new FileWriter(filename);
        CSVPrinter cp = new CSVPrinter(fw, CSVFormat.DEFAULT);
        Random r = new Random(System.currentTimeMillis());

        for (int i = 0; i < numberOfRows; i++) {
            cp.printRecord(rowTypes.get(r.nextInt(rowTypes.size())));
        }

        cp.flush();
        cp.close();
    }

    public static void printCSV(List<List<String>> rows, String filename) throws IOException {
        FileWriter fw = new FileWriter(filename);
        CSVPrinter cp = new CSVPrinter(fw, CSVFormat.DEFAULT);

        for (List<String> row : rows) {
            cp.printRecord(row);
        }

        cp.flush();
        cp.close();
    }

    public static List<List<String>> createRowTypes(int numberOfRowTypes, int numberOfColumns, int numberOfDifferentValues, int minChars, int maxChars) {
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


}
