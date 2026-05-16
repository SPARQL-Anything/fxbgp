package io.github.sparqlanything.fxbgp.stream.join.listeners;

import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import org.apache.jena.graph.Node;

public interface DataSourceContainerListener {

    public void onDataSourceContainer(DataSourceContainer dataSourceContainer);

}
