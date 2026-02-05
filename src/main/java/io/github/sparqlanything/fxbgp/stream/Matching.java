package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.FX;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Match;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Matching {
    private static final Logger L = LoggerFactory.getLogger(Matching.class);
    private Map<FXNode, List<Node>> map;
    private Map<Node, Set<FXNode>> nodesMap;
    private Map<FXNode,FX> componentsMap;
    private Set<FXNode> cursor;
    private boolean unresolvable = false;
    private List<Node> contextPath = new ArrayList<>();
    private List<Node> path = new ArrayList<>();

    /**
     * Matching can happen later
     *
     * @param cursor
     * @param nodePath
     * @param contextPath
     * @param path
     */
    Matching(FXNode cursor, List<Node> nodePath, List<Node> contextPath, List<Node> path) {
        if(cursor == null) throw new RuntimeException("cursor is null");
        if(!cursor.isRoot()) throw new RuntimeException("cursor is not root");
        this.cursor = new HashSet<>();
        this.map = new HashMap<>();
        this.contextPath = contextPath;
        this.path = path;
        populate(cursor);
        this.set(cursor, nodePath);
    }

    /**
     * Make copies
     *
     * @param map
     * @param cursor
     * @param contextPath
     * @param path
     */
    private Matching(Map<FXNode, List<Node>> map, Set<FXNode> cursor, List<Node> contextPath, List<Node> path, Map<Node, Set<FXNode>> nodesMap, Map<FXNode,FX> componentsMap) {
        this.map = map;
        this.cursor = cursor;
        this.contextPath = contextPath;
        this.path = path;
        this.nodesMap = nodesMap;
        this.componentsMap = componentsMap;
    }

    private void populate(FXNode cursor) {
        if(cursor.isRoot()) {
            nodesMap = new HashMap<>();
            componentsMap = new HashMap<>();
        }
        if(!nodesMap.containsKey(cursor)) {
            nodesMap.put(cursor.getNode(), new HashSet<>());
        }
        if(!componentsMap.containsKey(cursor.getNode())) {
            componentsMap.put(cursor, cursor.getAnnotation().getTerm());
        }
        nodesMap.get(cursor.getNode()).add(cursor);
        for(FXNode entry : cursor.getChildren()) {
            populate(entry);
        }
    }

    public Map<FXNode, List<Node>> getMap() {
        return Collections.unmodifiableMap(map);
    }

    public Map<FXNode, Node> getMatches() {
        Map<FXNode, Node> matches = new HashMap<>();
        for(Map.Entry<FXNode, List<Node>> entry : map.entrySet()){
            FXNode node = entry.getKey();
            List<Node> vals = entry.getValue();
            matches.put(node, vals.get(vals.size() -1));
        }
        return Collections.unmodifiableMap(matches);
    }

    public Set<FXNode> getCursor() {
        return Collections.unmodifiableSet(cursor);
    }

    public boolean contains(FXNode patternNode) {
        return map.containsKey(patternNode);
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    public int size() {
        return this.map.size();
    }

    private void unset(FXNode patternNode) {
        this.map.remove(patternNode);
        if(isContainer(patternNode)) {
            // Also remove its predicate
            this.map.remove(patternNode.getParent());
            this.cursor.remove(patternNode);
            // And put cursor on the previous container
            this.cursor.add(patternNode.getParent().getParent());
        }else if(isValueOrTypeOrRoot(patternNode)) {
            // Remove it and set its parent
            this.map.remove(patternNode.getParent());
            this.cursor.remove(patternNode);
            // Remove its predicate
            this.map.remove(patternNode.getParent());
            // Place the cursor on its container
            this.cursor.add(patternNode.getParent().getParent());
        } else if(isPredicate(patternNode)) {
            this.cursor.remove(patternNode);
            this.cursor.add(patternNode.getParent());
        }
    }

    public void set(FXNode patternNode, List<Node> valuePath) {
        this.map.put(patternNode, valuePath);
        if(!patternNode.isRoot()) {
            if(isContainer(patternNode)) {
                // Remove its parent
                this.cursor.remove(patternNode.getParent());
                this.cursor.add(patternNode);
            }else if(isValueOrTypeOrRoot(patternNode)) {
                // Remove its parent and step backward
                this.cursor.remove(patternNode.getParent());
                this.cursor.add(patternNode.getParent().getParent());
            } else if(isPredicate(patternNode)) {
                this.cursor.remove(patternNode.getParent());
                this.cursor.add(patternNode);
            }
        } else {
            this.cursor.add(patternNode);
        }
    }

    public boolean isValueOrTypeOrRoot(FXNode patternNode) {
        FX t = componentsMap.get(patternNode);
        return t.equals(FX.Type) || t.equals(FX.Value) || t.equals(FX.Root);
    }

    public boolean isContainer(FXNode patternNode) {
        FX t = componentsMap.get(patternNode);
        return t.equals(FX.Container);
    }

    public boolean isPredicate(FXNode patternNode) {
        FX t = componentsMap.get(patternNode);
        return t.equals(FX.SlotNumber) || t.equals(FX.SlotString) || t.equals(FX.TypeProperty);
    }

    public boolean isValueOrTypeOrRoot(FX t) {
        return t.equals(FX.Type) || t.equals(FX.Value) || t.equals(FX.Root);
    }

    public boolean isContainer(FX t) {
        return t.equals(FX.Container);
    }

    public boolean isPredicate(FX t) {
        return t.equals(FX.SlotNumber) || t.equals(FX.SlotString) || t.equals(FX.TypeProperty);
    }

    public Matching copy(){
        return new Matching(new HashMap(this.map), new HashSet<>(this.cursor), this.contextPath, this.path, this.nodesMap, this.componentsMap);
    }

    public Set<Matching> check(Node node, FX component) {
        Set<Matching> spawned = new HashSet<>();
        // Cursor is last matched node in the tree pattern
        // Check if the coming node matches any following FXNode
        Set<FXNode> matched = new HashSet<>();
        for(FXNode c: cursor){
            for(FXNode newCursor: c.getChildren()){
                if(componentsMap.get(newCursor).equals(component) &&
                        nodeMatches(newCursor.getNode(), node)){
                    // Verify the values are in the right path
                    List<Node> cursorValue = map.get(c);
                    if(path.subList(0, path.size() - 1).equals(cursorValue)){
                        matched.add(newCursor);
                    }
                }
            }
        }

        // No match following this path
        if(matched.size() == 0) {
            // Do nothing
            // If cursor is a single container, ignore, otherwise, discard
            if(cursor.size() == 1 && componentsMap.get(cursor.iterator().next()).equals(FX.Container )) {

            } else if(isPredicate(componentsMap.get(cursor.iterator().next()))){
                // If cursors are predicates, since the value didn't match,
                // Restore container
                FXNode unmatchedPredicate = cursor.iterator().next(); // get the reference of one predicate
                for(FXNode old: cursor){
                    // Remove all cursors
                    map.remove(old);
                }
                cursor = new HashSet<>();
                cursor.add(unmatchedPredicate.getParent());
                // This may generate duplicates (but we'll remove them later)
            } else{
                this.unresolvable = true;
            }
            return Collections.emptySet();
        }

        // If a single cursor is matched, set it and spawn
        if(matched.size() == 1){
            // Partial matches are corrupted (e.g. if the cursor contains more than one
            // predicate, but only one matches the object, discard)
            if(cursor.size() != 1){
                this.unresolvable = true;
            } else {
                // We set the match and spawn, if needed
                //spawned.addAll(setAndSpawn(matched));
                spawned = setAndSpawn(matched);
            }
        }else if(matched.size() > 1){
            // We spawn all combinations and unset this matching
            // Group by distinct equal nodes (to tackle the case of two same variables in predicate position)
            Map<Node, Set<FXNode>> dn = new HashMap<>();
            for(FXNode c: matched){
                if(!dn.containsKey(c.getNode())){
                    dn.put(c.getNode(), new HashSet<>());
                }
                dn.get(c.getNode()).add(c);
            }
            // Take the keys and generate combinations from 1 up to their len
            List input = new ArrayList<>();
            for(Set<FXNode> nn: dn.values()){
                input.add(new ArrayList<>(nn));
            }
            List<List> subsets = FXTreeUtils.subsets(input);
            for(List s: subsets){
                if(s.isEmpty()){
                    continue;
                }
                Matching m = copy();
                for(Object sn: s){
                    for(FXNode c: (List<FXNode>) sn){
                        m.set(c, new ArrayList<>(path));
                    }
                }
                spawned.add(m);
            }
            // We spawn all possible combinations
            this.unresolvable = true;
        }


        // Check if the matching is partial, i.e. there is one or more unpending slots
        // One way to detect this is to check that all cursors are of the same type
        if(cursor.size() > 1){
            for(FXNode c1: cursor){
                for(FXNode c2: cursor){
                    if(!componentsMap.get(c1).equals(componentsMap.get(c2))){
                        this.unresolvable = true;
                    }
                }
            }
        }

        // Check orphan variables.
        // For example, if one FXNode/?var is bound but a different FXNode/?var is not, mark as unresolvable
        if(!unresolvable) {
            for (Map.Entry<FXNode, List<Node>> entry : map.entrySet()) {
                Node matchingNode = entry.getKey().getNode();
                for (FXNode fxn : nodesMap.get(matchingNode)) {
                    if (!map.containsKey(fxn) || !map.get(fxn).equals(entry.getValue())) {
                        this.unresolvable = true;
                        break;
                    }
                }
                if (this.unresolvable) {
                    break;
                }
            }
        }
        if(L.isDebugEnabled()) {
            logSpawned(spawned);
        }
        return spawned;
    }

    private void logSpawned(Set<Matching> spawned){
        if(spawned.isEmpty()){
            return;
        }
        StringBuilder sb = new StringBuilder();
        for(Matching m: spawned){
            sb.append(m.hashCode() + " ");
        }
        L.debug("  ... {} generates {} and {}.", this.hashCode(), sb.toString(), unresolvable != true ? "survives" : "dies");
    }

    private Set<Matching> spawn(Set<FXNode> matched){
        Set<Matching> spawned = new HashSet<>();
        for(FXNode match: matched){
            FX component  = match.getAnnotation().getTerm();
            // Set the matching cursor
            if(isContainer(component)) {
                // If matched FXNode is Container object
                // (actually it is always object, because root never gets checked)
                // How many matched cursors?
                Matching m = copy();
                m.unset(match);
                spawned.add(m);
            }else if(isValueOrTypeOrRoot(component)) {
                Matching m = copy();
                m.unset(match);
                spawned.add(m);
            }else if(isPredicate(component)) {
                // Nothing to do
            }
        }
        return spawned;
    }

    private Set<Matching> setAndSpawn(Set<FXNode> matched){
        //Set<Matching> spawned = new HashSet<>();
        for(FXNode c: matched){
            this.set(c, new ArrayList<>(path));
        }
        return spawn(matched);
    }

    public void endContainer(){
        // Check if the node we are leaving is bound to any match to the container we are leaving.
        // If it does, mark it as unresolvable
        if(map.values().contains(path)){
            this.unresolvable = true;
        }
    }

    public boolean isUnresolvable() {
        return unresolvable;
    }

    public static boolean nodeMatches(Node patternNode, Node dataNode){
        if(patternNode.isBlank()){
            return true;
        }
        if(patternNode.isVariable()){
            return true;
        }
        if(patternNode.isURI() && patternNode.sameTermAs(dataNode)){
            return true;
        }
        if(patternNode.isLiteral() && patternNode.sameValueAs(dataNode)){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.getMap().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Matching) {
            return this.getMap().equals(((Matching) obj).getMap());
        }
        return false;
    }
}
