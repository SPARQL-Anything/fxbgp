package io.github.sparqlanything.fxbgp.joins.utils.impl;

import io.github.sparqlanything.fxbgp.joins.utils.TestCase;
import io.github.sparqlanything.fxbgp.joins.utils.TestCaseExecutor;
import io.github.sparqlanything.fxbgp.stream.join.ContainerSelector;
import io.github.sparqlanything.fxbgp.stream.join.listeners.impl.DataSourceContainerCollectorListenerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.ContainerIsomorphism;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.parsers.StreamParser;
import org.apache.jena.graph.Node;
import org.junit.Assert;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TestCaseExecutorImpl implements TestCaseExecutor {

    @Override
    public void assertEquals(TestCase testCase) {

        StreamParser streamParser = testCase.getParser();
        DataSourceContainerCollectorListenerImpl listener = testCase.getListener();
        streamParser.parse();
        Set<DataSourceContainer> sourceContainers = listener.getCollectedContainers();
        Set<ContainerSelector> selectors = testCase.getContainerSelectors();
        Assert.assertFalse(selectors.isEmpty());
        Collection<ContainerIsomorphism> isomorphisms = new HashSet<>();
        Set<Map<Node, Node>> expectedBindings = testCase.getBindings();

        for (ContainerSelector selector : selectors) {
            for (DataSourceContainer dataSourceContainer : sourceContainers) {
                Collection<ContainerIsomorphism> bindings = selector.matches(dataSourceContainer);

                if (expectedBindings.isEmpty())
                    Assert.assertNull(bindings);

                if (!expectedBindings.isEmpty() && bindings != null) {
                    Assert.assertNotNull(bindings);
                    isomorphisms.addAll(bindings);
                }
            }
        }

        Set<Map<Node, Node>> actualBindings = new HashSet<>();
        for (ContainerIsomorphism containerIsomorphism : isomorphisms) {
            actualBindings.add(containerIsomorphism.asMap());
        }

        Assert.assertEquals(expectedBindings, actualBindings);
    }


}
