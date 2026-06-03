package io.github.sparqlanything.fxbgp.stream.join.parsers;

import io.github.sparqlanything.fxbgp.stream.join.listeners.DataSourceContainerListener;


public interface StreamParser {

    public void parse();

    public DataSourceContainerListener getDataSourceContainerListener();
}
