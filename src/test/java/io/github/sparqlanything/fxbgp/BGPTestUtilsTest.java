package io.github.sparqlanything.fxbgp;

import org.apache.jena.sparql.core.BasicPattern;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.io.IOException;

public class BGPTestUtilsTest {

    @Rule
    public TestName name =  new TestName();

    @Test
    public void n1() throws IOException {
        BasicPattern bg = BGPTestUtils.readBGP("stream/"+name.getMethodName());
        System.out.println(bg);
    }

    @Test
    public void space1() throws IOException {
        BasicPattern bg = BGPTestUtils.readBGP(name.getMethodName());
        System.out.println(bg);
    }


    @Test
    public void space2() throws IOException {
        BasicPattern bg = BGPTestUtils.readBGP(name.getMethodName());
        System.out.println(bg);
    }


    @Test
    public void space3() throws IOException {
        BasicPattern bg = BGPTestUtils.readBGP(name.getMethodName());
        System.out.println(bg);
    }
}
