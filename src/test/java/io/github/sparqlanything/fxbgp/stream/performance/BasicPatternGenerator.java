package io.github.sparqlanything.fxbgp.stream.performance;

import com.google.common.collect.Sets;
import io.github.sparqlanything.fxbgp.FXNode;
import io.github.sparqlanything.fxbgp.FXTriplePattern;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.Var;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BasicPatternGenerator {

//    private static final FXNode container = new FXNode(FX.Container, NodeGenerator.variableGenerator);
//    private static final FXNode slotNumber = new FXNode(FX.SlotNumber, NodeGenerator.slotNumberGenerator);
//    private static final FXNode typeProperty = new FXNode(FX.Type, NodeGenerator.typePropertyGenerator);
//    private static final FXNode root = new FXNode(FX.Root, NodeGenerator.rootGenerator);
//    private static final FXNode slotString = new FXNode(FX.SlotString, new NodeGenerator.xyzPredicateGenerator(Set.of("a")));
//    private static final FXNode type = new FXNode(FX.SlotString, new NodeGenerator.xyzPredicateGenerator(Set.of("t")));
//    private static final FXNode value = new FXNode(FX.SlotString, new NodeGenerator.ValueGenerator(Set.of("a")));

    private final FXNode container, slotNumber, typeProperty, root, value;

    public BasicPatternGenerator(FXNode container, FXNode slotNumber, FXNode typeProperty, FXNode root, FXNode value) {
        this.container = container;
        this.slotNumber = slotNumber;
        this.typeProperty = typeProperty;
        this.root = root;
        this.value = value;
    }


    public BasicPattern generateContainerConcretePattern(Map<FXTriplePattern, Integer> description) {

        List<Triple> triples = new ArrayList<>();
        Node containerNode = container.getNodeGenerator().getVariableNode();

        description.forEach((tp, n) -> {
            List<Node> objectNodes = new ArrayList<>(tp.getObject().getNodeGenerator().getConcreteNodes(n));
            List<Node> predicateNodes = new ArrayList<>(tp.getPredicate().getNodeGenerator().getConcreteNodes(n));
            for (int i = 0; i < n; i++) {
                triples.add(Triple.create(containerNode, predicateNodes.get(i), objectNodes.get(i)));
            }
        });

        return new BasicPattern(triples);
    }

    public Set<BasicPattern> insertVariables(BasicPattern pattern, int numberOfVariables) {
        Set<BasicPattern> result = new HashSet<>();
        int concreteNodes = pattern.size() * 2;
        Set<Integer> setsOfNodesIDs = IntStream.range(0, concreteNodes).boxed().collect(Collectors.toSet());
        Set<Set<Integer>> setsOfVariablesIDs = Sets.powerSet(setsOfNodesIDs);
        for (Set<Integer> element : setsOfVariablesIDs) {
            if (element.size() != numberOfVariables - 1) {
                continue;
            }
            List<Triple> triples = new ArrayList<>();
            for (int i = 0; i < concreteNodes; i += 2) {
                Triple t = pattern.getList().get(i / 2);
                Node s = t.getSubject();
                Node p = t.getPredicate();
                if (element.contains(i)) {
                    p = Var.alloc("p_" + i);
                }
                Node o = t.getObject();
                if (element.contains(i + 1)) {
                    o = Var.alloc("o_" + (i + 1));
                }
                triples.add(Triple.create(s, p, o));
            }
            result.add(new BasicPattern(triples));
        }


        return result;
    }

    public Set<BasicPattern> getSxSDistinctNodesWithSlotNumber(int numberOfPatterns, int numberOfVariables) {

        Map<FXTriplePattern, Integer> patterns = new HashMap<>();
        patterns.put(new FXTriplePattern(container, slotNumber, value), numberOfPatterns);
        BasicPattern pattern = generateContainerConcretePattern(patterns);
        if(pattern.isEmpty())
            return new HashSet<>();

        return new HashSet<>(insertVariables(pattern, numberOfVariables));
    }

    public Set<BasicPattern> getSxSDistinctNodesWithSlotNumberAndRoot(int numberOfPatterns, int numberOfVariables) {

        Map<FXTriplePattern, Integer> patterns = new HashMap<>();
        patterns.put(new FXTriplePattern(container, slotNumber, value), numberOfPatterns - 1);
        patterns.put(new FXTriplePattern(container, typeProperty, root), 1);
        BasicPattern pattern = generateContainerConcretePattern(patterns);

        return new HashSet<>(insertVariables(pattern, numberOfVariables));
    }


}
