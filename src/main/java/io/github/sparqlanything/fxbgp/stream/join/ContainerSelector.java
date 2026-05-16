package io.github.sparqlanything.fxbgp.stream.join;

import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.ContainerBinding;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternRoot;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternType;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternTypeProperty;

public interface ContainerSelector {

    public ContainerBinding matches(DataSourceContainer container);

    public void setRootTriplePattern(TriplePatternTypeProperty rootTypeProperty, TriplePatternRoot triplePatternRoot);
    public void addTypeTriplePattern(TriplePatternTypeProperty typeProperty, TriplePatternType triplePatternType);

}
