package io.github.sparqlanything.fxbgp.stream.join.model.triplepattern;

import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;

public interface TriplePatternContainer extends TriplePatternObject, TriplePatternSubject {

    public boolean matchId(DataSourceContainer dataSourceContainer, ContainerBinding containerBinding);

}
