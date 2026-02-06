package io.github.sparqlanything.fxbgp.stream;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.File;
import java.io.IOException;

public class RunTest {
    @Rule
    public TestName testName = new TestName();
    private String input;
    private String bp;

    @Test
    public void test1_csv_s1() throws IOException, NotATreeException {
        prepare(testName.getMethodName());
        execute();
    }

    @Ignore
    @Test
    public void runProfile() throws IOException, NotATreeException {
        String path = "/Users/ed4565/Development/sparql-anything/fxstream-test/in/customers-10000.csv";
        this.bp =getClass().getClassLoader().getResource("./stream/all.easybgp").getPath();
        this.input = new File( path ).getPath();
        execute();
    }

    private void prepare(String methodName){
        String[] spl = testName.getMethodName().split("_");
        String inputName = spl[0] + "." + spl[1];
        String easyBGPName = spl[2];
        this.input = getClass().getClassLoader().getResource("./stream/" + inputName).getPath();
        this.bp = getClass().getClassLoader().getResource("./stream/" + easyBGPName + ".easybgp").getPath();
    }

    private void execute() throws IOException, NotATreeException {
        Run.main(new String[]{this.bp, this.input});
    }
}
