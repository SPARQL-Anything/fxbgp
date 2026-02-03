package io.github.sparqlanything.fxbgp.stream;

import java.util.ArrayList;
import java.util.Collections;
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
    private List<Node> contextPath;
    private List<Node> path;

    public FXQuerySolutionBuilder(FXTreePattern pattern, Set<QuerySolution> solutions) {
        this.pattern = pattern;
        this.string = pattern.toString();
        this.solutions = solutions;
        this.matches = new ArrayList<>();
        this.contextPath = new ArrayList<>();
        this.path = new ArrayList<>();
    }

    @Override
    public void startContainer(Node container) {
        super.startContainer(container);
        path.add(container);
        contextPath.add(container);
        match(container, FX.Container);
    }

    @Override
    public void onSlotNumber(Node predicate) {
        super.onSlotNumber(predicate);
        path.add(predicate);
        match(predicate, FX.SlotNumber);
    }

    @Override
    public void onSlotString(Node predicate) {
        super.onSlotString(predicate);
        path.add(predicate);
        match(predicate, FX.SlotString);
    }

    @Override
    public void endContainer() {
        if(L.isDebugEnabled()){
            beginEndContainer();
        }
        super.endContainer();
        triggerEndContainer();
        contextPath.remove(contextPath.size() - 1);
        path.remove(path.size() - 1);
        if(!path.isEmpty()) {
            // Go the prev container...
            path.remove(path.size() - 1);
        }
        if(L.isDebugEnabled()) {
            endEndContainer();
        }
    }

    @Override
    public void onTypeProperty() {
        super.onTypeProperty();
        path.add(RDF.type.asNode());
        match(RDF.type.asNode(), FX.TypeProperty);
    }

    @Override
    public void onType(Node type) {
        super.onType(type);
        path.add(type);
        match(type, FX.Type);
        // Step backward
        path.remove(path.size() - 1);
        path.remove(path.size() - 1);
    }

    @Override
    public void onTypeRoot() {
        super.onTypeRoot();
        Node fxr = NodeFactory.createURI(Triplifier.FACADE_X_TYPE_ROOT);
        path.add(fxr);
        match(fxr, FX.Root);
        // Step backward
        path.remove(path.size() - 1);
        path.remove(path.size() - 1);
    }

    @Override
    public void onValue(Node value) {
        super.onValue(value);
        path.add(value);
        match(value, FX.Value);
        // Step backward
        path.remove(path.size() - 1);
        path.remove(path.size() - 1);
    }

    private void match(Node node, FX component){
        if(L.isDebugEnabled()) {
            beginMatch(node, component);
        }
        Set<Matching> spawned = new HashSet<>();
        // Does the node match the current node in the tree pattern?
        if(component.equals(FX.Container) &&
                Matching.nodeMatches(pattern.getRoot().getNode(), node)){
            Matching newMatching = new Matching(pattern.getRoot(), new ArrayList<>(path), Collections.unmodifiableList(contextPath), Collections.unmodifiableList(path));
            if(matches.isEmpty()){
                matches.add(newMatching);
                endMatch(node, component);
                return;
            }else{
                spawned.add(newMatching);
            }
        }
        Set<Matching> removable = new HashSet<>();
        for(Matching matching: matches){
            Set<Matching> spawn = matching.check(node, component);
            if(spawn.size() > 0){
                L.debug(" --> {} spawned", spawn.size());
            }
            spawned.addAll(spawn);
            if(matching.isUnresolvable()){
                removable.add(matching);
            }
            L.trace("[end] checking against");
        }
        this.matches.removeAll(removable);
        this.matches.addAll(spawned);

        // Remove duplicates (hash code possibly changed)!
        this.matches = new ArrayList<>(new HashSet<>(this.matches));
        Set<Matching> completed = new HashSet<>();
        for(Matching matching: matches) {
            if (matching.getMap().size() == pattern.getSize()) {
                addQuerySolution(matching);
                completed.add(matching);
            }
        }
        this.matches.removeAll(completed);
        if(L.isDebugEnabled()) {
            endMatch(node, component);
        }
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
        for(Map.Entry<FXNode, Node> entry : matching.getMatches().entrySet()){
            if(L.isDebugEnabled()) {
                L.debug(" >>>>> {} {} <<<<<", entry.getKey(), entry.getValue());
                L.debug(" ----- {} {} <<<<<", entry.getKey(), entry.getKey().getNode().isVariable());
            }
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

    private static String TMP_LOG = null;
    private Node lastLogged = null;
    public void beginMatch(Node node, FX component){
       if(!(node.toString() + component).equals(TMP_LOG)){
           TMP_LOG = node.toString() + component.toString();
           L.debug("# [EVENT] - {} - {}", node, component.getName());
           L.debug("# - Path: {}", path);
           L.debug("# - Context: - {}", contextPath);
       }
       logPattern();
       L.debug(">> Before: {} matches", matches.size());
       logMatches();
    }

    public void endMatch(Node node, FX component){
        L.debug("<< After: {} matches", matches.size());
        logMatches();
    }

    private void logMatches(){
        for(Matching matching: matches){
            L.debug("  {}[{}] cursor: {} ", matching.hashCode(), matching.getMap().size(), matching.getCursor());
            for(Map.Entry<FXNode,List<Node>> entry: matching.getMap().entrySet()){
                L.debug("    {} >> {}", entry.getKey(), entry.getValue().get(entry.getValue().size()-1));
            }
        }

    }
    public void beginEndContainer(){
        if(!TMP_LOG.equals("endContainer")) {
            TMP_LOG = "endContainer";
            L.debug(" [EVENT END CONTAINER] ");
        }
        logPattern();
        L.debug("Before:");
        logMatches();
    }

    public void endEndContainer(){
        if (!FXTreeUtils.asTree(pattern.getRoot()).contains("SlotString")){
            L.debug("After:");
            logMatches();
        }
    }

    private void logPattern(){
        L.debug("## {} - {}", this.hashCode(), patternToString(this.pattern.getRoot()));
    }

    private String patternToString(FXNode node){
        StringBuilder sb = new StringBuilder();
        sb.append(node.toString());
        if(node.getChildren().size() > 0) {
            sb.append("[ ");
            for (FXNode ch : node.getChildren()) {
                sb.append(patternToString(ch));
                sb.append(" ");
            }
            sb.append("]");
        }
        return sb.toString();
    }
}
