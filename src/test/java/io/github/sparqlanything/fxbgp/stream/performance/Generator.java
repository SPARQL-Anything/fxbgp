package io.github.sparqlanything.fxbgp.stream.performance;

import com.google.common.collect.Sets;
import io.github.sparqlanything.fxbgp.*;
import io.github.sparqlanything.model.Triplifier;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.vocabulary.RDF;

import java.util.*;

public class Generator {

    private static final Set<FXTriplePattern> FX_PATTERNS_NO_TYPE = new HashSet<>();

    private static FXNode container = new FXNode(FX.Container, NodeGenerator.variableGenerator),
            slotNumber = new FXNode(FX.SlotNumber, NodeGenerator.slotNumberGenerator),
            typeProperty = new FXNode(FX.Type, NodeGenerator.typePropertyGenerator),
            root = new FXNode(FX.Root, NodeGenerator.rootGenerator),
            slotString = new FXNode(FX.SlotString, new NodeGenerator.xyzPredicateGenerator(Set.of("a"))),
            type = new FXNode(FX.SlotString, new NodeGenerator.xyzPredicateGenerator(Set.of("t"))),
            value = new FXNode(FX.SlotString, new NodeGenerator.ValueGenerator(Set.of("a")));

    static {
        // Compute FX Triple patterns
        FX_PATTERNS_NO_TYPE.add(new FXTriplePattern(container, slotNumber, value));
        FX_PATTERNS_NO_TYPE.add(new FXTriplePattern(container, slotNumber, container));
        FX_PATTERNS_NO_TYPE.add(new FXTriplePattern(container, slotString, value));
        FX_PATTERNS_NO_TYPE.add(new FXTriplePattern(container, slotString, container));
        FX_PATTERNS_NO_TYPE.add(new FXTriplePattern(container, typeProperty, root));
    }

    public static BasicPattern generateContainerPatterns(Map<FXTriplePattern, Integer> description) {

        List<Triple> triples = new ArrayList<>();
        Node containerNode = container.getNodeGenerator().getVariableNode();

        description.forEach((tp, n) -> {
            List<Node> predicateNodes = new ArrayList<>(tp.getPredicate().getNodeGenerator().getConcreteNodes(n));
            List<Node> objectNodes = new ArrayList<>(tp.getObject().getNodeGenerator().getConcreteNodes(n));
            for (int i = 0; i < n; i++) {
                triples.add(Triple.create(containerNode, predicateNodes.get(i), objectNodes.get(i)));
            }
        });

        return new BasicPattern(triples);
    }


    public static void generate(int numberOfTriplePatterns, Set<FXTriplePattern> fxTriplePatternSet) {

        List<Set<FXTriplePattern>> tps = new ArrayList<>(numberOfTriplePatterns);
        for (int i = 0; i < numberOfTriplePatterns; i++)
            tps.add(fxTriplePatternSet);

        // All possible FX triple patterns of size numberOfTriplePatterns
        Set<List<FXTriplePattern>> candidateTPs = Sets.cartesianProduct(tps);
        Set<Set<FXTriplePattern>> ctps = new HashSet<>();
        candidateTPs.forEach(tpss -> ctps.add(new HashSet<>(tpss)));
        System.out.println("Number of FX BGPs: " + ctps.size());

        Set<BasicPattern> bgps = new HashSet<>();
        for (Set<FXTriplePattern> fxBGP : ctps)
            bgps.addAll(generateBGP(fxBGP));
        System.out.println("Number of generated BGPs " + bgps.size());

        bgps.forEach(bgp -> System.out.println(bgp + "\n"));

        // Compute all possible combinations of TPs to Join. For example, consider a BGP of 3 TPs (TP1, TP2, TP3),
        // a way of joining all
        // of TPs is to join TP1 and TP2, and TP2 and TP3. Another way is to join TP1 and TP2, and TP1 and TP3
//        Set<Integer> tpIDs = IntStream.range(0, numberOfTriplePatterns).boxed().collect(Collectors.toSet());
//        Set<Set<Integer>> joins = Sets.powerSet(tpIDs);
//
//        // Select all possible combinations of joins such that
//        // |combination| >= 2 (This guarantees that join is valid (it joins two TPs))
//        // the union of combinations is equivalent to {0... numberOfTriplePatterns-1} (This guarantees that all the TPs are joined)
//        Set<Set<Integer>> possibleTPJoins = joins.stream().filter(s -> s.size() == 2).collect(Collectors.toSet());
//        Set<Set<Set<Integer>>> candidateBGPJoins = Sets.powerSet(possibleTPJoins);
//        Set<Set<Set<Integer>>> possibleBGPJoins = new HashSet<>();
//        for (Set<Set<Integer>> i : candidateBGPJoins) {
//            if (union(i).equals(tpIDs)) {
//                possibleBGPJoins.add(i);
//            }
//        }
//
//        Set<FXJoin> joinSet = Arrays.stream(FXJoin.values()).collect(Collectors.toSet());
//        Integer max = possibleBGPJoins.stream().map(Set::size).max(Integer::compareTo).get();
//
//        for (int i = 0; i < max; i++) {
//
//        }
//
//        for (Set<Set<Integer>> join : possibleBGPJoins) {
//
////            List<Set<FXJoin>> concreteJoins = new ArrayList<>();
////            for (int i = 0; i < join.size(); i++) {
////                concreteJoins.add(joinSet);
////            }
////            Set<List<FXJoin>> concreteJoin = Sets.cartesianProduct(concreteJoins);
////            for (BasicPattern pattern : bgps) {
////                BasicPattern newPattern = new BasicPattern(pattern);
////
////            }
//        }


    }

    private static final Comparator<Triple> ct = new Comparator<Triple>() {
        @Override
        public int compare(Triple o1, Triple o2) {
            int count = 0;
            if (o1.getSubject().isConcrete()) {
                count--;
            }

            if (o1.getPredicate().isConcrete()) {
                count--;
            }

            if (o1.getObject().isConcrete()) {
                count--;
            }
            if (o2.getSubject().isConcrete()) {
                count++;
            }

            if (o2.getPredicate().isConcrete()) {
                count++;
            }

            if (o2.getObject().isConcrete()) {
                count++;
            }

            return count;
        }
    };


    private static List<BasicPattern> generateBGP(Set<FXTriplePattern> fxBGP) {
        List<BasicPattern> result = new ArrayList<>();
        List<Set<Triple>> concreteTriples = new ArrayList<>();
        for (FXTriplePattern fxTriplePattern : fxBGP) {
            concreteTriples.add(realise(fxTriplePattern));
        }
        for (List<Triple> triples : Sets.cartesianProduct(concreteTriples)) {
            triples = new ArrayList<>(triples);
            triples.sort(ct);
            result.add(new BasicPattern(renameBGP(triples)));
        }
        return result;
    }


//    private static Triple joinTriples(FXJoin j, Triple t2, Triple t1) {
//        switch (j) {
//            case SxS -> {
//                t2 = Triple.create(t1.getSubject(), t2.getPredicate(), t2.getObject());
//            }
//            case PxP -> {
//                t2 = Triple.create(t2.getSubject(), t1.getPredicate(), t2.getObject());
//            }
//            case OxO -> {
//                t2 = Triple.create(t2.getSubject(), t2.getPredicate(), t1.getObject());
//            }
//            case SPxSP -> {
//                t2 = Triple.create(t1.getSubject(), t1.getPredicate(), t2.getObject());
//            }
//            case SOxSO -> {
//                t2 = Triple.create(t1.getSubject(), t2.getPredicate(), t1.getObject());
//            }
//            case POxPO -> {
//                t2 = Triple.create(t2.getSubject(), t1.getPredicate(), t1.getObject());
//            }
//            case PSxPO -> {
//                t2 = Triple.create(t2.getSubject(), t1.getPredicate(), t1.getSubject());
//            }
//            case SxO -> {
//                t2 = Triple.create(t2.getSubject(), t2.getPredicate(), t1.getSubject());
//            }
//        }
//        return t2;
//    }

    private static List<Triple> renameBGP(List<Triple> bgp) {
        int varCount = 1;
        int valueCount = 1;
        int iriCount = 1;
        List<Triple> triples = new ArrayList<>();
        for (Triple triple : bgp) {
            Node s = triple.getSubject();
            if (triple.getSubject().isVariable()) {
                s = NodeFactory.createVariable("s" + varCount++);
            }
            Node p = triple.getPredicate();
            if (triple.getPredicate().isVariable()) {
                p = NodeFactory.createVariable("p" + varCount++);
            } else if (triple.getPredicate().isURI()) {
                if (triple.getPredicate().getURI().startsWith(RDF.uri + "_")) {
                    p = RDF.li(iriCount++).asNode();
                } else if (triple.getPredicate().getURI().startsWith(Triplifier.XYZ_NS + "p_")) {
                    p = NodeFactory.createURI(Triplifier.XYZ_NS + "_" + iriCount++);
                }

            }

            Node o = triple.getObject();
            if (triple.getObject().isVariable()) {
                o = NodeFactory.createVariable("o" + varCount++);
            } else if (triple.getObject().isLiteral()) {
                o = NodeFactory.createLiteralByValue("" + valueCount++);
            } else if (triple.getObject().isURI()) {
                if (triple.getObject().getURI().startsWith(Triplifier.XYZ_NS + "p")) {
                    o = NodeFactory.createURI(Triplifier.XYZ_NS + "_" + iriCount++);
                }
            }
            triples.add(Triple.create(s, p, o));
        }
        return triples;
    }

    private static List<BasicPattern> getJoinedBGP(BasicPattern bgp, Set<Set<Integer>> idsOfTpsToBeJoined) {
        for (Set<Integer> tpsToBeJoined : idsOfTpsToBeJoined) {

        }
        return null;
    }


    private static Set<Triple> realise(FXTriplePattern tp) {
        Set<Triple> result = new HashSet<>();
        Set<Node> subjects = nodeToConcrete(tp.getSubject());
        Set<Node> predicates = nodeToConcrete(tp.getPredicate());
        Set<Node> objects = nodeToConcrete(tp.getObject());
        for (List<Node> nodes : Sets.cartesianProduct(subjects, predicates, objects)) {
            result.add(Triple.create(nodes.get(0), nodes.get(1), nodes.get(2)));
        }
        return result;
    }

    private static Set<Node> nodeToConcrete(FXNode node) {
        Set<Node> result = new HashSet<>();
        result.add(NodeFactory.createVariable("v"));
        if (node.equals(FX.SlotNumber)) {
            result.add(RDF.li(1).asNode());
        } else if (node.equals(FX.SlotString) || node.equals(FX.Type)) {
            result.add(NodeFactory.createURI(Triplifier.XYZ_NS + "p"));
        } else if (node.equals(FX.TypeProperty)) {
            result.add(RDF.type.asNode());
        } else if (node.equals(FX.Value)) {
            result.add(NodeFactory.createLiteralByValue("value"));
        } else if (node.equals(FX.Root)) {
            result.add(NodeFactory.createURI(Triplifier.FACADE_X_TYPE_ROOT));
        }
        return result;
    }


    private static Set<Integer> union(Set<Set<Integer>> sets) {
        Set<Integer> result = new HashSet<>();
        for (Set<Integer> i : sets) {
            result = Sets.union(result, i);
        }
        return result;
    }

    public static void main(String[] args) {
        //generate(1, FX_PATTERNS_NO_TYPE);
        Map<FXTriplePattern, Integer> patterns = new HashMap<>();
        patterns.put(new FXTriplePattern(container, slotNumber, value), 1);
        patterns.put(new FXTriplePattern(container, typeProperty, root), 1);
        BasicPattern pattern = generateContainerPatterns(patterns);
        System.out.println(pattern);
    }
}
