package io.github.sparqlanything.fxbgp.stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.model.Triplifier;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FXQuerySolutionBuilder extends FXAbstractNodeEventListener {
    private final Logger L = LoggerFactory.getLogger(FXQuerySolutionBuilder.class);
    private final String string;
    private Set<QuerySolution> solutions;
    private FXTreePattern pattern;
    private List<Matching> matches;
    private List<Node> path;

    public FXQuerySolutionBuilder(FXTreePattern pattern, Set<QuerySolution> solutions) {
        this.pattern = pattern;
        this.string = pattern.toString();
        this.solutions = solutions;
        this.matches = new ArrayList<>();
        this.path = new ArrayList<>();
    }

    @Override
    public void startContainer(Node container) {
        super.startContainer(container);
        path.add(container);
        match(container, FX.Container);
    }

    @Override
    public void onSlotNumber(Node predicate) {
        super.onSlotNumber(predicate);
        match(predicate, FX.SlotNumber);
    }

    @Override
    public void onSlotString(Node predicate) {
        super.onSlotString(predicate);
        match(predicate, FX.SlotString);
    }

    @Override
    public void endContainer() {
        super.endContainer();
        path.remove(path.size() - 1);
        triggerEndContainer();
    }

    @Override
    public void onTypeProperty() {
        super.onTypeProperty();
        match(RDF.type.asNode(), FX.TypeProperty);
    }

    @Override
    public void onType(Node type) {
        super.onType(type);
        match(type, FX.Type);
    }

    @Override
    public void onTypeRoot() {
        super.onTypeRoot();
        match(NodeFactory.createURI(Triplifier.FACADE_X_TYPE_ROOT), FX.Root);
    }

    @Override
    public void onValue(Node value) {
        super.onValue(value);
        match(value, FX.Value);
    }

    private void match(Node node, FX component){
//        L.info("=== {} ===", string);
//        L.info("node {} ({})", node, component.getName());
//        L.info("matches: {}", matches.size());

        // Does the node match the current node in the tree pattern?

        // Is it a container?
        if(this.matches.isEmpty()){ //this.matches.isEmpty() &&
            // We haven't matched any node yet
            // We take the root of the pattern, and attempt a match
            FXNode rnode = pattern.getRoot();
            // If the root node matches, register the match and set the cursor on it
            if(Matching.nodeMatches(rnode.getNode(), node)){
                Matching matching = new Matching(rnode, node);
                matches.add(matching);
                //registerMatch(matching, rnode, node);
            }
            // If it doesn't match, continue
            return;
        }else if(component.equals(FX.Container)){
            // Other containers that are not root
            // If it is a container, let's attempt a match from
            // the root of the pattern. If that works, we spawn
            // a new matching
            if(Matching.nodeMatches(pattern.getRoot().getNode(), node)){
                matches.add(new Matching(pattern.getRoot(), node,
                        new ArrayList(path)));
            }
        }

        Set<Matching> spawned = new HashSet<>();
        Set<Matching> completed = new HashSet<>();
        for(Matching matching: matches){
//            L.info(" {} >> {} ??", node, matching.getMap().size());
            Set<Matching> spawn = matching.check(node, component);
            spawned.addAll(spawn);
//            L.info(" {} << {} ??", node, matching.getMap().size());
            if(matching.getMap().size() == pattern.nodes().size()){
                addQuerySolution(matching);
                completed.add(matching);
            }
        }
//        L.info("completed {} spawned {}", completed.size(), spawned.size());
        this.matches.removeAll(completed);
        this.matches.addAll(spawned);
    }

    private void triggerEndContainer(){
        Set<Matching> unresolvable = new HashSet<>();
        for(Matching matching: matches){
            matching.endContainer();
            if(matching.isUnresolvable()){
                unresolvable.add(matching);
            }
        }
        // Remove unresolvable matches
        this.matches.removeAll(unresolvable);
    }

    private void addQuerySolution(Matching matching){
        Map<String, RDFNode> solution = new HashMap<>();
        for(Map.Entry<FXNode, Node> entry : matching.getMap().entrySet()){
//            L.info(" >>>>> {} {} <<<<<", entry.getKey(), entry.getValue());
//            L.info(" ----- {} {} <<<<<", entry.getKey(), entry.getKey().getNode().isVariable());
            if(entry.getKey().getNode().isVariable()){
                String var = entry.getKey().getNode().getName();
                RDFNode val = toRDFNode(entry.getValue());
                solution.put(var, val);
            }
        }
        solutions.add(FXQuerySolution.make(solution));
    }

    private RDFNode toRDFNode(Node n){
        if(n.isURI()){
            return ResourceFactory.createResource(n.getURI());
        }else if(n.isBlank()){
            // XXX I am not sure how to handle this at the moment
            return ResourceFactory.createResource(n.getBlankNodeLabel());
        }else if(n.isVariable()){
            throw new RuntimeException("Cannot be variable");
        } else if(n.isLiteral()){
            if (n.getLiteralLanguage() != null) {
                return ResourceFactory.createLangLiteral(n.getLiteralLexicalForm(), n.getLiteralLanguage());
            }else if(n.getLiteralDatatype() != null){
                return ResourceFactory.createTypedLiteral(n.getLiteralLexicalForm(), n.getLiteralDatatype());
            }else{
                return ResourceFactory.createPlainLiteral(n.getLiteralLexicalForm());
            }
        }
        throw new RuntimeException("This should never happen");
    }

}
