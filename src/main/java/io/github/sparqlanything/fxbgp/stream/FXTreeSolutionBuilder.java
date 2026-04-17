package io.github.sparqlanything.fxbgp.stream;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.model.Triplifier;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FXTreeSolutionBuilder extends FXAbstractNodeEventListener {
    private final static Logger L = LoggerFactory.getLogger(FXTreeSolutionBuilder.class);
    private final String string;
    private Set<Binding> solutions;
    private FXTreePattern pattern;
    private final Set<Matching> matchesA = new HashSet<>();
    private final Set<Matching> matchesB = new HashSet<>();
    private Set<Matching> matches = matchesA;
    private final PathAccessor accessor;
    private Node dataSourceNode = null;
    private boolean troubleshoot = L.isDebugEnabled();
    public FXTreeSolutionBuilder(FXTreePattern pattern, Set<Binding> solutions,
                                 PathAccessor accessor) {
        this.pattern  = pattern;
        this.string   = pattern.toString();
        this.solutions = solutions;
        this.accessor  = accessor;
    }

    /**
     * We initialise when any data source is triggered
     */
    private void init(){
        matchesA.clear();
        matchesB.clear();
        this.matches = matchesA;
    }

    private Set<Matching> swapBuffer() {
        Set<Matching> current = matches;
        matches = (matches == matchesA) ? matchesB : matchesA;
        matches.clear();
        return current;
    }

    private boolean matchedDataSource(){
        return !pattern.isGraphPattern() || this.dataSourceNode != null;
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

    @Override
    public void startContainer(Node container) {
        if(!matchedDataSource()) return;
        super.startContainer(container);
        match(container, FX.Container);
    }

    @Override
    public void onSlotNumber(Node predicate) {
        if(!matchedDataSource()) return;
        super.onSlotNumber(predicate);
        match(predicate, FX.SlotNumber);
    }

    @Override
    public void onSlotString(Node predicate) {
        if(!matchedDataSource()) return;
        super.onSlotString(predicate);
        match(predicate, FX.SlotString);
    }

    @Override
    public void endContainer() {
        if(!matchedDataSource()) return;
        if(troubleshoot) beginEndContainer();
        super.endContainer();
        triggerEndContainer();
        if(troubleshoot) endEndContainer();
    }

    @Override
    public void onTypeProperty() {
        if(!matchedDataSource()) return;
        super.onTypeProperty();
        match(RDF.type.asNode(), FX.TypeProperty);
    }

    @Override
    public void onType(Node type) {
        if(!matchedDataSource()) return;
        super.onType(type);
        match(type, FX.Type);
    }

    @Override
    public void onTypeRoot() {
        if(!matchedDataSource()) return;
        super.onTypeRoot();
        match(NodeFactory.createURI(Triplifier.FACADE_X_TYPE_ROOT), FX.Root);
    }

    @Override
    public void onValue(Node value) {
        if(!matchedDataSource()) return;
        super.onValue(value);
        match(value, FX.Value);
    }

    private void match(Node node, FX component){
        if(!matchedDataSource()) {
            if(troubleshoot) L.debug("Not this data source");
            return; //
        }
        if(troubleshoot) {
            beginMatch(node, component);
        }
        if(!pattern.containsComponent(component)) {
            Set<Matching> current = swapBuffer();
            for(Matching m : current){
                m.noMatchOnPath();
                matches.add(m);
            }
            return;
        }
        Matching newMatch = null;
        // Does the node match the current node in the tree pattern?
        if(component.equals(FX.Container) &&
                Matching.nodeMatches(pattern.getRoot().getNode(), node)){
            Matching newMatching = new Matching(pattern.getRoot(),
                    accessor.copyCurrentPath(), accessor);
            if(matches.isEmpty()){
                matches.add(newMatching);
                if(troubleshoot){
                    endMatch(node, component);
                }
                return;
            }else{
                newMatch = newMatching;
            }
        }

        Set<Matching> current = swapBuffer();
        if(newMatch != null){
            addOrComplete(newMatch);
        }
        long prefixHash = accessor.currentPrefixHash();
        for (Matching matching : current) {  // iterate the snapshot
            for (Matching s : matching.check(node, component, prefixHash)) {
                addOrComplete(s);
            }
            if (!matching.isUnresolvable()) {
                addOrComplete(matching);      // safe: check() done, hash is now stable
            }
        }
        if(troubleshoot) endMatch(node, component);
    }

    private void addOrComplete(Matching m) {
        if (m.size() == pattern.getSize()) addQuerySolution(m);
        else matches.add(m);
    }
    
    private void triggerEndContainer(){
        Set<Matching> current = swapBuffer();
        for(Matching matching: current){
            matching.endContainer();
            if(!matching.isUnresolvable()){
                matches.add(matching);
            }
        }
    }

    private void addQuerySolution(Matching matching){
        BindingBuilder solution = BindingBuilder.create();
        for(Map.Entry<FXNode, PathRecord> entry : matching.getMatches().entrySet()){
            if(entry.getKey().getNode().isVariable()){
                Node var_ = entry.getKey().getNode();
                Var var = Var.alloc(var_.getName());
                Node val = entry.getValue().getNode();
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
        Binding binding = solution.build();
        if(troubleshoot) {
            L.debug(" EMIT > {} <", binding);
        }
        solutions.add(binding);

    }

    private static String TMP_LOG = null;

    public void beginMatch(Node node, FX component){
       if(!(node.toString() + component).equals(TMP_LOG)){
           TMP_LOG = node + component.toString();
           L.debug("# [EVENT]:  ({} , {})", node, component.getName());
           L.debug("# - Path: {}", accessor.currentPath());
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
            L.debug("  Active match {} ({} bindings assigned), cursor: {} ", matching.getMatches().hashCode(), matching.getMatches().size(), matching.getCursor());
            for(Map.Entry<FXNode, PathRecord> entry: matching.getMatches().entrySet()){
                L.debug("   - binding   {} >> {}", entry.getKey(), entry.getValue().getNode());
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
        L.debug("## TSP {} - {}", this.hashCode(), patternToString(this.pattern.getRoot()));
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
