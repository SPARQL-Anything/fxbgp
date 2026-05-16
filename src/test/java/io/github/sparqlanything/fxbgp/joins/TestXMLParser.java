package io.github.sparqlanything.fxbgp.joins;

import io.github.sparqlanything.fxbgp.BGPTestUtils;
import io.github.sparqlanything.fxbgp.stream.join.listeners.impl.DataSourceContainerCollectorListenerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceContainerImpl;
import io.github.sparqlanything.fxbgp.stream.join.parsers.XMLParser;
import io.github.sparqlanything.model.IRIArgument;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class TestXMLParser {

    @Rule
    public TestName name = new TestName();

    private String getInputFilename(String methodName) throws URISyntaxException {
        URL url = BGPTestUtils.class.getClassLoader().getResource("./joins/xml/" + methodName + ".xml");
        Assert.assertNotNull(url);
        return url.toURI().toString();
    }

    @Test
    public void test1() throws URISyntaxException {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.LOCATION.toString(), getInputFilename(name.getMethodName()));
        DataSourceContainerCollectorListenerImpl listener = new DataSourceContainerCollectorListenerImpl();
        XMLParser xmlParser = new XMLParser(properties, listener);
        xmlParser.parse();
        Set<DataSourceContainer> expected = new HashSet<>();
        expected.add(new DataSourceContainerImpl("/A/B_1", properties));
        expected.add(new DataSourceContainerImpl("/A/B_2", properties));
        expected.add(new DataSourceContainerImpl("/A/B_2/C_1", properties));
        expected.add(new DataSourceContainerImpl("/A", properties, true));
        Assert.assertEquals(expected, listener.getCollectedContainers());
    }


    @Test
    public void test2() throws URISyntaxException {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.LOCATION.toString(), getInputFilename(name.getMethodName()));
        DataSourceContainerCollectorListenerImpl listener = new DataSourceContainerCollectorListenerImpl();
        XMLParser xmlParser = new XMLParser(properties, listener);
        xmlParser.parse();
        Set<DataSourceContainer> expected = new HashSet<>();
        expected.add(new DataSourceContainerImpl("/A/B_1", properties));
        expected.add(new DataSourceContainerImpl("/A/D_3", properties));
        expected.add(new DataSourceContainerImpl("/A/B_2", properties));
        expected.add(new DataSourceContainerImpl("/A/B_2/C_1", properties));
        expected.add(new DataSourceContainerImpl("/A/B_2/C_2", properties));
        expected.add(new DataSourceContainerImpl("/A", properties, true));
        Assert.assertEquals(expected.size(), listener.getCollectedContainers().size());
        Assert.assertEquals(expected, listener.getCollectedContainers());
    }

    @Test
    public void test3() throws URISyntaxException {
        Properties properties = new Properties();
        properties.setProperty(IRIArgument.LOCATION.toString(), getInputFilename(name.getMethodName()));
        DataSourceContainerCollectorListenerImpl listener = new DataSourceContainerCollectorListenerImpl();
        XMLParser xmlParser = new XMLParser(properties, listener);
        xmlParser.parse();
        Set<DataSourceContainer> expected = new HashSet<>();
        expected.add(new DataSourceContainerImpl("/A/B_1", properties));
        expected.add(new DataSourceContainerImpl("/A/D_3", properties));
        expected.add(new DataSourceContainerImpl("/A/B_2", properties));
        expected.add(new DataSourceContainerImpl("/A/B_2/C_1", properties));
        expected.add(new DataSourceContainerImpl("/A/B_2/C_2", properties));
        expected.add(new DataSourceContainerImpl("/A/B_2/C_2/E_1", properties));
        expected.add(new DataSourceContainerImpl("/A", properties, true));
        Assert.assertEquals(expected.size(), listener.getCollectedContainers().size());
        Assert.assertEquals(expected, listener.getCollectedContainers());
    }
}
