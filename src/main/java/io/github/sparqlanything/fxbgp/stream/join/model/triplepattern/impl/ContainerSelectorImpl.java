package io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.impl;

import com.google.common.collect.Sets;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceContainer;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceFXRoot;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceType;
import io.github.sparqlanything.fxbgp.stream.join.model.datasource.DataSourceTypeProperty;
import io.github.sparqlanything.fxbgp.stream.join.model.triplepattern.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ContainerSelectorImpl implements ContainerSelector {

    private final TriplePatternContainer triplePatternContainer;

    // type property for fx:Root assertion
    private TriplePatternTypeProperty rootTypeProperty;
    private TriplePatternRoot triplePatternRoot;
    private boolean mustBeRoot = false;

    // types
    private final Map<TriplePatternTypeProperty, Set<TriplePatternType>> triplePatternType = new HashMap<>();

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
        Set<TriplePatternType> s = this.triplePatternType.get(typeProperty);
        if (s == null)
            s = new HashSet<>();
        s.add(triplePatternType);
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

        AtomicBoolean match = new AtomicBoolean(true);
        Set<ContainerBinding> matching = new HashSet<>();
        Set<DataSourceType> dataSourceTypes = dataSourceContainer.getTypes();

        for (Map.Entry<TriplePatternTypeProperty, Set<TriplePatternType>> e: triplePatternType.entrySet()){
            // check that all types in e.getValue() are types in data source container
            if(!dataSourceTypes.containsAll(e.getValue())){
                match.set(false);
                break;
            }

            if(e.getKey().getBGPNode().isVariable()){
            }
            // if so possible variables are matched
        }


        return match.get();
    }


    private boolean matchTypePropertyTypeTriplePattern(ContainerBinding containerBinding, DataSourceContainer dataSourceContainer, TriplePatternTypeProperty tp, TriplePatternType t) {
        // ?s:C ?p:TP ?o:T
        if (tp.getBGPNode().isVariable())
            containerBinding.set(tp, DataSourceTypeProperty.rdfType);

        return false;

    }

    private boolean matchTypePropertyRootTriplePatterns(ContainerBinding containerBinding, DataSourceContainer dataSourceContainer) {

        if ((mustBeRoot && !dataSourceContainer.isRoot()) || (!mustBeRoot && dataSourceContainer.isRoot()))
            return false;

        if (mustBeRoot && rootTypeProperty.getBGPNode().isVariable())
            containerBinding.set(rootTypeProperty, DataSourceTypeProperty.rdfType);

        if (mustBeRoot && triplePatternRoot.getBGPNode().isVariable())
            containerBinding.set(triplePatternRoot, DataSourceFXRoot.fxRoot);

        return true;
    }
}
