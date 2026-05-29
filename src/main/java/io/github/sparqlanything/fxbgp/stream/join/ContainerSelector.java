package io.github.sparqlanything.fxbgp.stream.join;

import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXNodeAnnotation;
import io.github.sparqlanything.fxbgp.stream.join.model.ModelException;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.ContainerIsomorphism;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternRoot;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternType;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.TriplePatternTypeProperty;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.TriplePatternContainerImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.TriplePatternRootImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.TriplePatternTypeImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.TriplePatternTypePropertyImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import java.util.*;

public interface ContainerSelector {

    static Collection<ContainerSelector> getSelectors(FXBGPAnnotation bpa, Properties properties) {
        Map<Node, ContainerSelectorImpl.Builder> containerSelectorBuilders = new HashMap<>();

        for (Triple triple : bpa.getOpBGP().getPattern().getList()) {
            ContainerSelectorImpl.Builder containerSelector = containerSelectorBuilders.get(triple.getSubject());
            if (containerSelector == null) {
                TriplePatternContainerImpl triplePatternContainerImpl = new TriplePatternContainerImpl(triple.getSubject(), properties);
                containerSelector = new ContainerSelectorImpl.Builder(triplePatternContainerImpl);
                containerSelectorBuilders.put(triple.getSubject(), containerSelector);
            }

            FXNodeAnnotation predicateAnnotation = bpa.getAnnotation(triple.getPredicate());
            FXNodeAnnotation objectAnnotation = bpa.getAnnotation(triple.getObject());
            if (predicateAnnotation.getTerm() == FX.TypeProperty) {
                TriplePatternTypeProperty triplePatternTypeProperty = new TriplePatternTypePropertyImpl(triple.getPredicate(), properties);
                if (objectAnnotation.getTerm() == FX.Root) {
                    TriplePatternRoot triplePatternRoot = new TriplePatternRootImpl(triple.getObject(), properties);
                    containerSelector.setRootTriplePattern(triplePatternTypeProperty, triplePatternRoot);
                } else if (objectAnnotation.getTerm() == FX.Type) {
                    TriplePatternType triplePatternType = new TriplePatternTypeImpl(triple.getObject(), properties);
                    containerSelector.addTypeTriplePattern(triplePatternTypeProperty, triplePatternType);
                } else {
                    throw new ModelException("Unexpected FXType (" + objectAnnotation.getTerm() + ") for object in triple " + triple);
                }
            }
        }

        Collection<ContainerSelector> result = new HashSet<>();
        containerSelectorBuilders.values().forEach(builder -> result.add(builder.build()));
        return result;
    }


    public Collection<ContainerIsomorphism> matches(DataSourceContainer container);

}
