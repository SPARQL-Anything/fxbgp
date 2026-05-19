package io.github.sparqlanything.fxbgp.stream.join.listeners.impl;

import io.github.sparqlanything.fxbgp.stream.join.listeners.DataSourceContainerListener;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;

import java.util.HashSet;
import java.util.Set;

public class DataSourceContainerCollectorListenerImpl implements DataSourceContainerListener {

    protected final Set<DataSourceContainer> collectedContainers = new HashSet<>();

    @Override
    public void onDataSourceContainer(DataSourceContainer dataSourceContainer) {
        collectedContainers.add(dataSourceContainer);
    }

    public Set<DataSourceContainer> getCollectedContainers() {
        return collectedContainers;
    }
}
