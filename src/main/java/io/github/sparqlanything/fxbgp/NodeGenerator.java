package io.github.sparqlanything.fxbgp;

import io.github.sparqlanything.model.Triplifier;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.RDF;

import java.util.*;

public interface NodeGenerator {

    NodeGenerator variableGenerator = new NodeGenerator() {
        @Override
        public List<Node> getConcreteNodes(int numberOfNodes) {
            return List.of();
        }
    };

    NodeGenerator slotNumberGenerator = new NodeGenerator() {

        @Override
        public List<Node> getConcreteNodes(int numberOfNodes) {
            List<Node> result = new ArrayList<>();
            for (int i = 1; i < numberOfNodes + 1; i++) {
                result.add(RDF.li(i).asNode());
            }
            return result;
        }
    };

    NodeGenerator slotStringGenerator = new NodeGenerator() {

        @Override
        public List<Node> getConcreteNodes(int numberOfNodes) {
            List<Node> result = new ArrayList<>();
            for (int i = 1; i < numberOfNodes + 1; i++) {
                result.add(NodeFactory.createURI(Triplifier.XYZ_NS + "f" + (i - 1)));
            }
            return result;
        }
    };

    NodeGenerator typePropertyGenerator = new NodeGenerator() {
        @Override
        public List<Node> getConcreteNodes(int numberOfNodes) {
            return List.of(RDF.type.asNode());
        }
    };

    NodeGenerator rootGenerator = new NodeGenerator() {
        @Override
        public List<Node> getConcreteNodes(int numberOfNodes) {
            return List.of(NodeFactory.createURI(Triplifier.FACADE_X_TYPE_ROOT));
        }
    };

    default Set<Node> getVariableNodes(int numberOfNodes) {
        Set<Node> result = new HashSet<>();
        for (int i = 0; i < numberOfNodes; i++) {
            result.add(Var.alloc("c_" + i));
        }
        return result;
    }

    default Node getVariableNode() {
        return getVariableNodes(1).iterator().next();
    }

    public List<Node> getConcreteNodes(int numberOfNodes);

    default Node getConcreteNode() {
        return getConcreteNodes(1).iterator().next();
    }

    default Set<Node> getNodes(int numberOfVariables, int numberOfConcrete) {
        Set<Node> result = new HashSet<>();
        result.addAll(getVariableNodes(numberOfVariables));
        result.addAll(getConcreteNodes(numberOfConcrete));
        return result;
    }

    class xyzPredicateGenerator implements NodeGenerator {
        private final Set<String> predicateNames;

        public xyzPredicateGenerator(Set<String> predicateNames) {
            this.predicateNames = predicateNames;
        }

        @Override
        public List<Node> getConcreteNodes(int numberOfNodes) {
            List<Node> result = new ArrayList<>();
            List<String> names = new ArrayList<>(predicateNames);
            Collections.shuffle(names);
            int nextValueToPick = 0;
            for (int i = 0; i < numberOfNodes; i++) {
                if (nextValueToPick == names.size()) {
                    nextValueToPick = 0;
                }
                result.add(NodeFactory.createURI(Triplifier.XYZ_NS + names.get(nextValueToPick++)));
            }
            return result;
        }
    }


    class ValueGenerator implements NodeGenerator {
        private final Set<String> values;

        public ValueGenerator(Set<String> values) {
            this.values = values;
        }

        @Override
        public List<Node> getConcreteNodes(int numberOfNodes) {
            List<Node> result = new ArrayList<>();
            List<String> valuesOrdered = new ArrayList<>(values);
            Collections.shuffle(valuesOrdered);
            int nextValueToPick = 0;
            for (int i = 0; i < numberOfNodes; i++) {
                if (nextValueToPick == values.size()) {
                    nextValueToPick = 0;
                }
                result.add(NodeFactory.createLiteralString(valuesOrdered.get(nextValueToPick++)));
            }
            return result;
        }
    }


    class OrderedValueGenerator implements NodeGenerator {
        private final List<String> values;

        public OrderedValueGenerator(List<String> values) {
            this.values = values;
        }

        @Override
        public List<Node> getConcreteNodes(int numberOfNodes) {
            List<Node> result = new ArrayList<>();
            int nextValueToPick = 0;
            for (int i = 0; i < numberOfNodes; i++) {
                if (nextValueToPick == values.size()) {
                    nextValueToPick = 0;
                }
                result.add(NodeFactory.createLiteralString(values.get(nextValueToPick++)));
            }
            return result;
        }
    }
}
