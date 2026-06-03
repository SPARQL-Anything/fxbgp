package io.github.sparqlanything.fxbgp.joins.utils.impl;

import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.fxbgp.joins.utils.TestCase;
import io.github.sparqlanything.model.IRIArgument;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;
import java.util.Set;

public class CSVTestCase extends TestCaseBase implements TestCase {

    private final String testCaseName;
    private final Set<List<FX>> patterns;

    public CSVTestCase(String testCaseName, Set<List<FX>> patterns) {
        this.testCaseName = testCaseName;
        this.patterns = patterns;
    }

    @Override
    protected String getInputFilepath() {
        return String.format("test-cases/%s/input.csv", testCaseName);
    }

    @Override
    protected String getQueryFilepath() {
        return String.format("test-cases/%s/query.easybgp", testCaseName);
    }

    @Override
    protected String getBindingsFilepath() {
        return String.format("test-cases/%s/results.csv", testCaseName);
    }

    @Override
    protected String getFormat() {
        return "csv";
    }

    @Override
    public Set<List<FX>> getPatterns() {
        return patterns;
    }

    @Override
    public Properties getProperties() {
        Properties properties = new Properties();
        try {
            properties.setProperty(IRIArgument.LOCATION.toString(), getFileInputURL().toURI().toString());
            return properties;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
