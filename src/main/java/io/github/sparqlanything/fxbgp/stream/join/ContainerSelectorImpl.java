package io.github.sparqlanything.fxbgp.stream.join;

import com.google.common.collect.Lists;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceFXRoot;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceType;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceTypeProperty;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.*;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.ContainerIsomorphismImpl;

import java.util.*;

public class ContainerSelectorImpl implements ContainerSelector {

    private final TriplePatternContainer triplePatternContainer;

    // type property for fx:Root assertion
    private TriplePatternTypeProperty rootTypeProperty;
    private TriplePatternRoot triplePatternRoot;
    private boolean mustBeRoot = false;

    // types
    private final List<POPattern> triplePatternType = new ArrayList<>();

    public ContainerSelectorImpl(TriplePatternContainer triplePatternContainer) {
        this.triplePatternContainer = triplePatternContainer;
    }

    private Map<TriplePatternSlot, TriplePatternObject> slotTriplePatternObjectMap;

    public void setRootTriplePattern(TriplePatternTypeProperty rootTypeProperty, TriplePatternRoot triplePatternRoot) {
        Objects.requireNonNull(rootTypeProperty);
        Objects.requireNonNull(triplePatternRoot);
        mustBeRoot = true;
        this.rootTypeProperty = rootTypeProperty;
        this.triplePatternRoot = triplePatternRoot;
    }

    public void addTypeTriplePattern(TriplePatternTypeProperty typeProperty, TriplePatternType triplePatternType) {
        Objects.requireNonNull(typeProperty);
        Objects.requireNonNull(triplePatternType);
        this.triplePatternType.add(new POPattern(typeProperty, triplePatternType));
    }

    @Override
    public Collection<ContainerIsomorphism> matches(DataSourceContainer dataSourceContainer) {

        ContainerIsomorphism containerBinding = new ContainerIsomorphismImpl();

        // single binding
        if (!(matchId(dataSourceContainer, containerBinding)
                && matchTypePropertyRootTriplePatterns(containerBinding, dataSourceContainer)))

            return null;

        // multiple binding
        Set<ContainerIsomorphism> bindings = new HashSet<>();
        bindings.add(containerBinding);
        if (!matchTypePropertyTypeTriplePatterns(dataSourceContainer, bindings))
            return null;


        return bindings;
    }

    private boolean matchTypePropertyTypeTriplePatterns(DataSourceContainer dataSourceContainer, Set<ContainerIsomorphism> bindings) {

        if (this.triplePatternType.isEmpty())
            return true;

        List<List<DataSourceType>> listOfListsOfTypes = new ArrayList<>(this.triplePatternType.size());
        this.triplePatternType.forEach(po -> listOfListsOfTypes.add(new ArrayList<>(dataSourceContainer.getTypes())));
        List<List<DataSourceType>> listOfListsOfAssignments = Lists.cartesianProduct(listOfListsOfTypes);
        Set<ContainerIsomorphism> bindingsToBeAdded = new HashSet<>();
        // Check assignment
        for (List<DataSourceType> assignment : listOfListsOfAssignments) {
            boolean match = true;
            for (int i = 0; i < assignment.size(); i++) {
                // check assignment between assignment[i] and this.triplePatternType[i]
                if (canBeAssignedTo(assignment.get(i), triplePatternType.get(i))) {
                    for (ContainerIsomorphism containerBinding : bindings) {
                        ContainerIsomorphism containerBindingCopy = containerBinding.copy();
                        // assign rdf:type to predicate
                        containerBindingCopy.set(triplePatternType.get(i).predicate, DataSourceTypeProperty.rdfType);
                        // assign assignment[i] to object
                        containerBindingCopy.set(triplePatternType.get(i).object, assignment.get(i));
                        bindingsToBeAdded.add(containerBindingCopy);
                    }
                } else {
                    match = false;
                    break;
                }
            }
        }

        if (!bindingsToBeAdded.isEmpty()) {
            bindings.clear();
            bindings.addAll(bindingsToBeAdded);
        }


        return !bindingsToBeAdded.isEmpty();
    }

    private boolean canBeAssignedTo(DataSourceType dataSourceType, POPattern poPattern) {
        return poPattern.object.asNode().isVariable() ||
                poPattern.object.asNode().isBlank() ||
                poPattern.object.asNode().isURI() && poPattern.object.matches(dataSourceType);

    }


    private boolean matchTypePropertyTypeTriplePattern(ContainerIsomorphism containerBinding, DataSourceContainer dataSourceContainer, TriplePatternTypeProperty tp, TriplePatternType t) {
        // ?s:C ?p:TP ?o:T
        if (tp.asNode().isVariable())
            containerBinding.set(tp, DataSourceTypeProperty.rdfType);

        return false;

    }

    private boolean matchTypePropertyRootTriplePatterns(ContainerIsomorphism containerBinding, DataSourceContainer dataSourceContainer) {

        if ((mustBeRoot && !dataSourceContainer.isRoot()))
            return false;

        if (mustBeRoot) {
            containerBinding.set(rootTypeProperty, DataSourceTypeProperty.rdfType);
            containerBinding.set(triplePatternRoot, DataSourceFXRoot.fxRoot);
        }
        return true;
    }

    private boolean matchId(DataSourceContainer dataSourceContainer, ContainerIsomorphism containerBinding) {
        if (triplePatternContainer.asNode().isVariable() || triplePatternContainer.asNode().isBlank() || (triplePatternContainer.asNode().isURI() && dataSourceContainer.getId().equals(triplePatternContainer.getSurface()))) {
            containerBinding.set(triplePatternContainer, dataSourceContainer);
            return true;
        }
        return false;

    }

    @Override
    public String toString() {

        String stringBuilder = "{ " +
                "TPC: " +
                triplePatternContainer +
                " ; " +
                "RTP: " +
                rootTypeProperty +
                " ; " +
                "FXROOT: " +
                triplePatternRoot +
                " ; " +
                "Root?: " +
                mustBeRoot +
                " ; " +
                "CTPT: " +
                this.triplePatternType +
                " }";


        return stringBuilder;
    }

    static class POPattern {
        TriplePatternPredicate predicate;
        TriplePatternObject object;

        public POPattern(TriplePatternPredicate predicate, TriplePatternObject object) {
            this.predicate = predicate;
            this.object = object;
        }

        @Override
        public String toString() {
            return "POPattern{" +
                    "predicate=" + predicate +
                    ", object=" + object +
                    '}';
        }
    }

}
