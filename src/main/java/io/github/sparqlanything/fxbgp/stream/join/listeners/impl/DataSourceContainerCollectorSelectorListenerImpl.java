package io.github.sparqlanything.fxbgp.stream.join.listeners.impl;

import io.github.sparqlanything.fxbgp.stream.join.ContainerSelector;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.ContainerBinding;

import java.util.Objects;

public class DataSourceContainerCollectorSelectorListenerImpl extends DataSourceContainerCollectorListenerImpl {

    private final ContainerSelector containerSelector;

    public DataSourceContainerCollectorSelectorListenerImpl(ContainerSelector containerSelector) {
        super();
        Objects.requireNonNull(containerSelector);
        this.containerSelector = containerSelector;
    }

    @Override
    public void onDataSourceContainer(DataSourceContainer dataSourceContainer) {
        ContainerBinding binding = containerSelector.matches(dataSourceContainer);
        if (binding != null)
            collectedContainers.add(dataSourceContainer);
    }

}
