package io.github.sparqlanything.fxbgp.joins.utils;

import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.fxbgp.stream.join.ContainerSelector;
import io.github.sparqlanything.fxbgp.stream.join.listeners.impl.DataSourceContainerCollectorListenerImpl;
import io.github.sparqlanything.fxbgp.stream.join.parsers.StreamParser;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.BasicPattern;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public interface TestCase {

    StreamParser getParser();

    BasicPattern getBasicPattern();

    Set<Map<Node, Node>> getBindings();

    Set<ContainerSelector> getContainerSelectors();

    Properties getProperties();

    DataSourceContainerCollectorListenerImpl getListener();

    Set<List<FX>> getPatterns();

    String getTestCaseName();
}