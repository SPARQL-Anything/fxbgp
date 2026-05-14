package io.github.sparqlanything.fxbgp.stream.join.model.triplepattern;

import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;

public interface ContainerSelector {

    public ContainerBinding matches(DataSourceContainer container);

    public void setRootTriplePattern(TriplePatternTypeProperty rootTypeProperty, TriplePatternRoot triplePatternRoot);

}
