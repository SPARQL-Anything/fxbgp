package io.github.sparqlanything.fxbgp.stream.join;

import com.google.common.collect.Lists;
import io.github.sparqlanything.fxbgp.stream.join.model.ContainerIsomorphism;
import io.github.sparqlanything.fxbgp.stream.join.model.ContainerIsomorphismImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.*;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.impl.DataSourceSlotNumberImpl;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.*;

import java.util.*;

public class ContainerSelectorImpl implements ContainerSelector {

    private final TriplePatternContainer triplePatternContainer;

    // type property for fx:Root assertion
    private TriplePatternTypeProperty rootTypeProperty;
    private TriplePatternRoot triplePatternRoot;
    private boolean mustBeRoot = false;

    // types
    private final List<POPattern<TriplePatternTypeProperty, TriplePatternType>> triplePatternType = new ArrayList<>();
    private final List<POPattern<TriplePatternSlotNumber, TriplePatternValueOrContainer>> concreteSlotNumberPatterns = new ArrayList<>();
    private final List<POPattern<TriplePatternSlotNumber, TriplePatternValueOrContainer>> variableSlotNumberPatterns = new ArrayList<>();

    private ContainerSelectorImpl(TriplePatternContainer triplePatternContainer) {
        this.triplePatternContainer = triplePatternContainer;
    }

    public static class Builder {

        ContainerSelectorImpl containerSelector;

        public Builder(TriplePatternContainer triplePatternContainer) {
            containerSelector = new ContainerSelectorImpl(triplePatternContainer);
        }

        public Builder setRootTriplePattern(TriplePatternTypeProperty rootTypeProperty, TriplePatternRoot triplePatternRoot) {
            Objects.requireNonNull(rootTypeProperty);
            Objects.requireNonNull(triplePatternRoot);
            containerSelector.mustBeRoot = true;
            containerSelector.rootTypeProperty = rootTypeProperty;
            containerSelector.triplePatternRoot = triplePatternRoot;
            return this;
        }

        public Builder addTypeTriplePattern(TriplePatternTypeProperty typeProperty, TriplePatternType triplePatternType) {
            Objects.requireNonNull(typeProperty);
            Objects.requireNonNull(triplePatternType);
            containerSelector.triplePatternType.add(new POPattern<>(typeProperty, triplePatternType));
            return this;
        }

        public Builder addSlotNumberTriplePattern(TriplePatternSlotNumber slotNumber, TriplePatternValueOrContainer triplePatternValueOrContainer) {
            Objects.requireNonNull(slotNumber);
            Objects.requireNonNull(triplePatternValueOrContainer);
            if (slotNumber.asNode().isURI()) {
                containerSelector.concreteSlotNumberPatterns.add(new POPattern<>(slotNumber, triplePatternValueOrContainer));
            } else {
                containerSelector.variableSlotNumberPatterns.add(new POPattern<>(slotNumber, triplePatternValueOrContainer));
            }
            return this;
        }

        public ContainerSelector build() {
            // sort patterns by slot number
//            containerSelector.concreteSlotNumberPatterns.sort(new Comparator<>() {
//                @Override
//                public int compare(POPattern o1, POPattern o2) {
//                    return o1.predicate.compareTo(o2.predicate);
//                }
//            });
            return containerSelector;
        }


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

        if (!matchSlotNumberPattern(dataSourceContainer, bindings))
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
                if (!canBeAssignedTo(assignment.get(i), triplePatternType.get(i))) {
                    match = false;
                    break;
                }
            }
            if (match) {
                for (ContainerIsomorphism containerBinding : bindings) {

                    ContainerIsomorphism containerBindingCopy = containerBinding.copy();
                    boolean toDiscard = false;
                    for (int i = 0; i < assignment.size() && !toDiscard; i++) {
                        // assign rdf:type to predicate
                        toDiscard = !containerBindingCopy.set(triplePatternType.get(i).predicate, DataSourceTypeProperty.rdfType);
                        // assign assignment[i] to object
                        toDiscard = !containerBindingCopy.set(triplePatternType.get(i).object, assignment.get(i)) || toDiscard;
                    }
                    if (!toDiscard)
                        bindingsToBeAdded.add(containerBindingCopy);
                }
            }
        }

        if (!bindingsToBeAdded.isEmpty()) {
            bindings.clear();
            bindings.addAll(bindingsToBeAdded);
        }

        return !bindingsToBeAdded.isEmpty();
    }


    private boolean matchSlotNumberPattern(DataSourceContainer dataSourceContainer, Set<ContainerIsomorphism> bindings) {

        if (this.concreteSlotNumberPatterns.isEmpty())
            return true;


        return matchSlotNumberConcrete(dataSourceContainer, bindings);
    }

    private boolean matchSlotNumberConcrete(DataSourceContainer dataSourceContainer, Set<ContainerIsomorphism> containerIsomorphisms) {

        if (this.concreteSlotNumberPatterns.isEmpty())
            return true;

        // Check all the TPs (Container, SlotNumber, Object) with slot number URI match with the datasource slot numbers
        Map<DataSourceSlotNumber, DataSourceValueOrContainer> datasourceSlotNumber = dataSourceContainer.getSlotNumber();
        boolean match = true;
        Map<TriplePatternNode, DataSourceFXElement> bindingsToAdd = new HashMap<>();
        for (POPattern<TriplePatternSlotNumber, TriplePatternValueOrContainer> concreteSlotNumberPattern : this.concreteSlotNumberPatterns) {
            DataSourceValueOrContainer dataSourceValueOrContainer = datasourceSlotNumber.get(concreteSlotNumberPattern.predicate);
            if (!canBeAssignedTo(dataSourceValueOrContainer, concreteSlotNumberPattern)) {
                match = false;
                break;
            }
            bindingsToAdd.put(concreteSlotNumberPattern.predicate, new DataSourceSlotNumberImpl(concreteSlotNumberPattern.predicate.getNumber(), concreteSlotNumberPattern.predicate.getProperties()));
            bindingsToAdd.put(concreteSlotNumberPattern.object, dataSourceValueOrContainer);
        }

        if (!match)
            return false;

        // Add bindings for all the TPs (Container, SlotNumber, Object)
        for (ContainerIsomorphism containerIsomorphism : containerIsomorphisms) {
            containerIsomorphism.putAll(bindingsToAdd);
        }

        return true;
    }

    private boolean matchSlotNumberVariable(DataSourceContainer dataSourceContainer, Set<ContainerIsomorphism> containerIsomorphisms) {

        if (this.variableSlotNumberPatterns.isEmpty())
            return true;

        List<List<Map.Entry<DataSourceSlotNumber, DataSourceValueOrContainer>>> listOfListsOfSlotFillers = new ArrayList<>(this.variableSlotNumberPatterns.size());
        this.variableSlotNumberPatterns.forEach(po -> listOfListsOfSlotFillers.add(new ArrayList<>(dataSourceContainer.getSlotNumber().entrySet())));
        List<List<Map.Entry<DataSourceSlotNumber, DataSourceValueOrContainer>>> listOfListsOfAssignments = Lists.cartesianProduct(listOfListsOfSlotFillers);
        Set<ContainerIsomorphism> bindingsToBeAdded = new HashSet<>();

        for (List<Map.Entry<DataSourceSlotNumber, DataSourceValueOrContainer>> assignments : listOfListsOfAssignments) {
            boolean assignmentMatches = true;
            for (int i = 0; i < assignments.size(); i++) {
                // check assignment between assignment[i] and this.triplePatternType[i]
                if (!canBeAssignedTo(assignments.get(i).getValue(), this.variableSlotNumberPatterns.get(i))) {
                    assignmentMatches = false;
                    break;
                }
            }
            if (assignmentMatches) {
                for (ContainerIsomorphism containerIsomorphism : containerIsomorphisms) {
                    ContainerIsomorphism containerBindingCopy = containerIsomorphism.copy();
                    boolean toDiscard = false;

                    // Assign variables
                    for (int i = 0; i < assignments.size() && !toDiscard; i++) {
                        // assign to the slot number
                        toDiscard = !containerBindingCopy.set(variableSlotNumberPatterns.get(i).predicate, assignments.get(i).getKey());
                        // assign assignment[i] to object
                        toDiscard = !containerBindingCopy.set(variableSlotNumberPatterns.get(i).object, assignments.get(i).getValue()) || toDiscard;
                    }

                    if (!toDiscard)
                        bindingsToBeAdded.add(containerBindingCopy);
                }
            }
        }

        if (!bindingsToBeAdded.isEmpty()) {
            containerIsomorphisms.clear();
            containerIsomorphisms.addAll(bindingsToBeAdded);
        }

        return true;
    }


    private boolean canBeAssignedTo(DataSourceFXElement object, POPattern<? extends TriplePatternPredicate, ? extends TriplePatternObject> poPattern) {
        return poPattern.object.asNode().isVariable() ||
                poPattern.object.asNode().isBlank() ||
                poPattern.object.matches(object);

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

        return "{ " +
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
    }

    static class POPattern<P extends TriplePatternPredicate, O extends TriplePatternObject> {
        P predicate;
        O object;

        public POPattern(P predicate, O object) {
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
