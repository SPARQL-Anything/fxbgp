package io.github.sparqlanything.fxbgp.stream.join;

import com.google.common.collect.Lists;
import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXNodeAnnotation;
import io.github.sparqlanything.fxbgp.stream.join.model.ModelException;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceFXRoot;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceType;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceTypeProperty;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.*;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl.*;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

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
    public ContainerBinding matches(DataSourceContainer dataSourceContainer) {

        ContainerBinding containerBinding = new ContainerBindingImpl();

        if (triplePatternContainer.matchId(dataSourceContainer, containerBinding)
                && matchTypePropertyRootTriplePatterns(containerBinding, dataSourceContainer))
            return containerBinding;

        return null;
    }

    private boolean matchTypePropertyTypeTriplePatterns(ContainerBinding containerBinding, DataSourceContainer dataSourceContainer) {
        /*

        case 1:
        (_, a:TP, t:T) [t] :::

        case 2:
        (_, ?p:TP, t:T) [t] ::: ?p = rdf:type

        case 3:
        (_, ?p:TP, t1:T) (_, ?p:TP, t2:T) [t1, t2] ::: ?p = rdf:type

        case 4:
        (_, ?p1:TP, t1:T) (_, ?p2:TP, t2:T) [t1, t2] ::: ?p1 = <rdf:type, ?p2 = rdf:type>

        case 5:
        (_, ?p1:TP, ?t1:T) (_, ?p2:TP, ?t2:T) [t1, t2] :::
            <?p1 = rdf:type, ?p2 = rdf:type, ?t1 = t1, ?t2 = ?t1>
            <?p1 = rdf:type, ?p2 = rdf:type, ?t1 = t2, ?t2 = ?t2>
            <?p1 = rdf:type, ?p2 = rdf:type, ?t1 = t1, ?t2 = ?t2>
            <?p1 = rdf:type, ?p2 = rdf:type, ?t1 = t2, ?t2 = ?t1>

        case N1:
        (_, a:TP, t1:T) [t] ::: X

        case N2:
        (_, a:TP, t1:T) (_, a:TP, t2:T) [t] ::: X

        case N3:
        (_, a:TP, t1:T) (_, a:TP, t2:T) [t1] ::: X

        case N4:
        (_, a:TP, t1:T) (_, a:TP, t2:T) [t1, t] ::: X

         */

        List<List<DataSourceType>> listOfListsOfTypes = new ArrayList<>(this.triplePatternType.size());
        this.triplePatternType.forEach(po -> listOfListsOfTypes.add(new ArrayList<>(dataSourceContainer.getTypes())));
        List<List<DataSourceType>> listOfListsOfAssignments = Lists.cartesianProduct(listOfListsOfTypes);
        // Check assignment
        for (List<DataSourceType> assignment : listOfListsOfAssignments) {
            boolean match = true;
            for (int i = 0; i < assignment.size(); i++) {
                // check assignment between assignment[i] and this.triplePatternType[i]
                if (canBeAssignedTo(assignment.get(i), triplePatternType.get(i))) {
                    if (triplePatternType.get(i).predicate.asNode().isVariable()) {
                        // assign rdf:type to predicate
                    }

                    if (triplePatternType.get(i).object.asNode().isVariable()) {
                        // assign assignment[i] to object
                    }

                } else {
                    match = false;
                    break;
                }
            }
        }

        //TODO
        return false;
    }

    private boolean canBeAssignedTo(DataSourceType dataSourceType, POPattern poPattern) {
        return poPattern.object.asNode().isVariable() ||
                poPattern.object.asNode().isBlank() ||
                poPattern.object.asNode().isURI() && poPattern.object.equals(dataSourceType);

    }


    private boolean matchTypePropertyTypeTriplePattern(ContainerBinding containerBinding, DataSourceContainer dataSourceContainer, TriplePatternTypeProperty tp, TriplePatternType t) {
        // ?s:C ?p:TP ?o:T
        if (tp.asNode().isVariable())
            containerBinding.set(tp, DataSourceTypeProperty.rdfType);

        return false;

    }

    private boolean matchTypePropertyRootTriplePatterns(ContainerBinding containerBinding, DataSourceContainer dataSourceContainer) {

        if ((mustBeRoot && !dataSourceContainer.isRoot()))
            return false;

        if (mustBeRoot && rootTypeProperty.asNode().isVariable())
            containerBinding.set(rootTypeProperty, DataSourceTypeProperty.rdfType);

        if (mustBeRoot && triplePatternRoot.asNode().isVariable())
            containerBinding.set(triplePatternRoot, DataSourceFXRoot.fxRoot);

        return true;
    }

    public static Collection<ContainerSelector> getSelectors(FXBGPAnnotation bpa, Properties properties) {
        Map<Node, ContainerSelector> containerSelectors = new HashMap<>();

        for (Triple triple : bpa.getOpBGP().getPattern().getList()) {
            ContainerSelector containerSelector = containerSelectors.get(triple.getSubject());
            if (containerSelector == null) {
                TriplePatternContainerImpl triplePatternContainerImpl = new TriplePatternContainerImpl(triple.getSubject(), properties);
                containerSelector = new ContainerSelectorImpl(triplePatternContainerImpl);
                containerSelectors.put(triple.getSubject(), containerSelector);
            }

            FXNodeAnnotation predicateAnnotation = bpa.getAnnotation(triple.getPredicate());
            FXNodeAnnotation objectAnnotation = bpa.getAnnotation(triple.getPredicate());
            if (predicateAnnotation.getTerm() == FX.TypeProperty) {
                TriplePatternTypeProperty triplePatternTypeProperty = new TriplePatternTypePropertyImpl(triple.getPredicate(), properties);
                if (objectAnnotation.getTerm() == FX.Root) {
                    TriplePatternRoot triplePatternRoot = new TriplePatternRootImpl(triple.getObject(), properties);
                    containerSelector.setRootTriplePattern(triplePatternTypeProperty, triplePatternRoot);
                } else if (objectAnnotation.getTerm() == FX.Type) {
                    TriplePatternType triplePatternType = new TriplePatternTypeImpl(triple.getObject(), properties);
                    containerSelector.addTypeTriplePattern(triplePatternTypeProperty, triplePatternType);
                } else {
                    throw new ModelException("Unexpected Type for object in triple " + triple);
                }
            }
        }

        return containerSelectors.values();
    }


    class POPattern {
        TriplePatternPredicate predicate;
        TriplePatternObject object;

        public POPattern(TriplePatternPredicate predicate, TriplePatternObject object) {
            this.predicate = predicate;
            this.object = object;
        }

    }

}
