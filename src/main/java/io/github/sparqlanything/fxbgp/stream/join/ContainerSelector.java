package io.github.sparqlanything.fxbgp.stream.join;

import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXNodeAnnotation;
import io.github.sparqlanything.fxbgp.stream.join.model.ContainerIsomorphism;
import io.github.sparqlanything.fxbgp.stream.join.model.ModelException;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.*;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.*;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import java.util.*;

public interface ContainerSelector {

    static Collection<ContainerSelector> getSelectors(FXBGPAnnotation bpa, Properties properties) {
        Map<Node, ContainerSelectorImpl.Builder> containerSelectorBuilders = new HashMap<>();

        for (Triple triple : bpa.getOpBGP().getPattern().getList()) {
            ContainerSelectorImpl.Builder containerSelectorBuilder = containerSelectorBuilders.get(triple.getSubject());
            if (containerSelectorBuilder == null) {
                TriplePatternContainerImpl triplePatternContainerImpl = new TriplePatternContainerImpl(triple.getSubject(), properties);
                containerSelectorBuilder = new ContainerSelectorImpl.Builder(triplePatternContainerImpl);
                containerSelectorBuilders.put(triple.getSubject(), containerSelectorBuilder);
            }

            FXNodeAnnotation predicateAnnotation = bpa.getAnnotation(triple.getPredicate());
            FXNodeAnnotation objectAnnotation = bpa.getAnnotation(triple.getObject());
            if (predicateAnnotation.getTerm() == FX.TypeProperty) {
                TriplePatternTypeProperty triplePatternTypeProperty = new TriplePatternTypePropertyImpl(triple.getPredicate(), properties);
                if (objectAnnotation.getTerm() == FX.Root) {
                    TriplePatternRoot triplePatternRoot = new TriplePatternRootImpl(triple.getObject(), properties);
                    containerSelectorBuilder.setRootTriplePattern(triplePatternTypeProperty, triplePatternRoot);
                } else if (objectAnnotation.getTerm() == FX.Type) {
                    TriplePatternType triplePatternType = new TriplePatternTypeImpl(triple.getObject(), properties);
                    containerSelectorBuilder.addTypeTriplePattern(triplePatternTypeProperty, triplePatternType);
                } else {
                    throw new ModelException("Unexpected FXType (" + objectAnnotation.getTerm() + ") for object in triple " + triple);
                }
            } else if (predicateAnnotation.getTerm() == FX.SlotNumber) {
                TriplePatternSlotNumber triplePatternSlotNumber = new TriplePatternSlotNumberImpl(triple.getPredicate(), properties);
                TriplePatternValueOrContainer object = getTriplePatternValueOrContainer(properties, triple, objectAnnotation);
                containerSelectorBuilder.addSlotNumberTriplePattern(triplePatternSlotNumber, object);
            }
        }

        Collection<ContainerSelector> result = new HashSet<>();
        containerSelectorBuilders.values().forEach(builder -> result.add(builder.build()));
        return result;
    }

    private static TriplePatternValueOrContainer getTriplePatternValueOrContainer(Properties properties, Triple triple, FXNodeAnnotation objectAnnotation) {
        TriplePatternValueOrContainer object = null;
        if (objectAnnotation.getTerm() == FX.Value) {
            object = new TriplePatternValueImpl(triple.getObject(), properties);
        } else if (objectAnnotation.getTerm() == FX.Container) {
            object = new TriplePatternContainerImpl(triple.getObject(), properties);
        } else {
            throw new ModelException("Unexpected FXType (" + objectAnnotation.getTerm() + ") for object in triple " + triple);
        }
        return object;
    }


    public Collection<ContainerIsomorphism> matches(DataSourceContainer container);

}
