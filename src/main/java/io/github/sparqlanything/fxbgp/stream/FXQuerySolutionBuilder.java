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
import org.apache.jena.graph.Node_Variable;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Match;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBase;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FXQuerySolutionBuilder extends FXAbstractNodeEventListener {
    private final Logger L = LoggerFactory.getLogger(FXQuerySolutionBuilder.class);
    private final String string;
    private Set<Binding> solutions;
    private FXTreePattern pattern;
    private List<Matching> matches;
    private List<Node> contextPath;
    private List<Node> path;
    private Node dataSourceNode = null;

    public FXQuerySolutionBuilder(FXTreePattern pattern, Set<Binding> solutions) {
        this.pattern = pattern;
        this.string = pattern.toString();
        this.solutions = solutions;
    }

    /**
     * We initialise when any data source is triggered
     */
    private void init(){
        this.matches = new ArrayList<>();
        this.contextPath = new ArrayList<>();
        this.path = new ArrayList<>();
    }

    @Override
    public void startDataSource(Node dataSource) {
        super.startDataSource(dataSource);
        if(this.pattern.isGraphPattern()){
            if(Matching.nodeMatches(this.pattern.getGraphPatternNode(), dataSource)){
                dataSourceNode = dataSource;
                init();
            }
        }else{
            init();
        }
    }

    private boolean matchedDataSource(){
        return !pattern.isGraphPattern() || this.dataSourceNode != null;
    }

    @Override
    public void startContainer(Node container) {
        if(!matchedDataSource()){
            return;
        }
        super.startContainer(container);
        path.add(container);
        contextPath.add(container);
        match(container, FX.Container);
    }

    @Override
    public void onSlotNumber(Node predicate) {
        if(!matchedDataSource()){
            return;
        }
        super.onSlotNumber(predicate);
        path.add(predicate);
        match(predicate, FX.SlotNumber);
    }

    @Override
    public void onSlotString(Node predicate) {
        if(!matchedDataSource()){
            return;
        }
        super.onSlotString(predicate);
        path.add(predicate);
        match(predicate, FX.SlotString);
    }

    @Override
    public void endContainer() {
        if(!matchedDataSource()){
            return;
        }
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
        if(!matchedDataSource()){
            return;
        }
        super.onTypeProperty();
        path.add(RDF.type.asNode());
        match(RDF.type.asNode(), FX.TypeProperty);
    }

    @Override
    public void onType(Node type) {
        if(!matchedDataSource()){
            return;
        }
        super.onType(type);
        path.add(type);
        match(type, FX.Type);
        // Step backward
        path.remove(path.size() - 1);
        path.remove(path.size() - 1);
    }

    @Override
    public void onTypeRoot() {
        if(!matchedDataSource()){
            return;
        }
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
        if(!matchedDataSource()){
            return;
        }
        super.onValue(value);
        path.add(value);
        match(value, FX.Value);
        // Step backward
        path.remove(path.size() - 1);
        path.remove(path.size() - 1);
    }

    private void match(Node node, FX component){
        if(!matchedDataSource()){
            return;
        }
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
        BindingBuilder solution = BindingBuilder.create();
        for(Map.Entry<FXNode, Node> entry : matching.getMatches().entrySet()){
            if(L.isDebugEnabled()) {
                L.debug(" >>>>> {} {} <<<<<", entry.getKey(), entry.getValue());
                L.debug(" ----- {} {} <<<<<", entry.getKey(), entry.getKey().getNode().isVariable());
            }
            if(entry.getKey().getNode().isVariable()){
                Node var_ = entry.getKey().getNode();
                Var var = Var.alloc(var_.getName());
                Node val = entry.getValue();
                // XXX How to do it better?
                if(!solution.contains(var)) {
                    solution.add(var, val);
                }else{
                    if(!solution.get(var).equals(val)){
                        throw new RuntimeException("This should not happen");
                    }
                }
            }
        }
        // If graph pattern, add variable and match
        if(pattern.isGraphPattern()) {
            Node graphPN = pattern.getGraphPatternNode();
            if(graphPN.isVariable()){
                // XXX How to do it better?
                solution.add(Var.alloc(graphPN.getName()), dataSourceNode);
            }
        }
        synchronized (solutions) {
            solutions.add(solution.build());
        }
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
