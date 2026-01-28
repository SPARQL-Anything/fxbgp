package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.fxbgp.FXNodeAnnotation;
import org.apache.jena.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Matching {
    private static final Logger L = LoggerFactory.getLogger(Matching.class);
    private Map<FXNode, List<Node>> map;
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
    private Matching(Map<FXNode, List<Node>> map, Set<FXNode> cursor, List<Node> contextPath, List<Node> path) {
        this.map = map;
        this.cursor = cursor;
        this.contextPath = contextPath;
        this.path = path;
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

//    public void jumpBack() {
//        Set<FXNode> next = new HashSet<>();
//        for (FXNode c : this.cursor) {
//            FXNode nc = null;
//            for (int i = 0; i < steps; i++) {
//                nc = c.getParent();
//            }
//            if(nc == null) {
//                throw new RuntimeException("cursor is null");
//            }
//            next.add(nc);
//        }
//        this.cursor = next;
//    }

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
            // XXX This should never happen...?
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
        FX t = patternNode.getAnnotation().getTerm();
        return t.equals(FX.Type) || t.equals(FX.Value) || t.equals(FX.Root);
    }

    public boolean isContainer(FXNode patternNode) {
        FX t = patternNode.getAnnotation().getTerm();
        return t.equals(FX.Container);
    }

    public boolean isPredicate(FXNode patternNode) {
        FX t = patternNode.getAnnotation().getTerm();
        return t.equals(FX.SlotNumber) || t.equals(FX.SlotString) || t.equals(FX.TypeProperty);
    }

    public Matching copy(){
        return new Matching(new HashMap(this.map), new HashSet<>(this.cursor), new ArrayList<>(this.contextPath), this.path);
    }

    public Set<Matching> check(Node node, FX component) {
        if(node.toString().contains("H2")) {
            L.info("{} {}", node, component);
        }
        L.trace("Path: {} ", path);
        L.trace("Context Path: {}", contextPath);
        Set<Matching> spawned = new HashSet<>();
        // Cursor is last matched node in the tree pattern
        // Check if the coming node matches any follower
        Set<FXNode> matched = new HashSet<>();
        for(FXNode c: cursor){
            for(FXNode newCursor: c.getChildren()){
                if(newCursor.getAnnotation().getTerm().equals(component) &&
                        nodeMatches(newCursor.getNode(), node)){
                    // Verify the values are in the right path
                    List<Node> cursorValue = map.get(c);
                    if(path.subList(0, path.size() - 1).equals(cursorValue)){
                        //L.trace("Path matching success: \n{}\n{}", path, cursorValue);
                        //show(getMatches());
                        matched.add(newCursor);
                    }
                }
            }
        }
        for(FXNode newCursor: matched){
            if (map.containsKey(newCursor)) {
                // This never happens ...
                throw new RuntimeException("This should never happen");
//                Matching m = copy();
//                m.set(newCursor, new ArrayList<>(path));
//                spawned.add(m);
            }
            set(newCursor, new ArrayList<>(path));
            // If value type or root, spawn and clear map keys
            if(isValueOrTypeOrRoot(newCursor) || isContainer(newCursor)) {
                Matching m = copy();
                m.unset(newCursor);
                spawned.add(m);
            } else {

            }
        }
        show(getMatches());
        return spawned;
    }

    public void endContainer(){
        // Check if the node we are leaving is bound to any match to the container we are leaving.

        // If it does, mark it as unresolvable
        if(map.values().contains(path)){
            this.unresolvable = true;
        }
    }

    private static void show(Map<FXNode, Node> mmm){
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<FXNode, Node> entry : mmm.entrySet()){
            sb.append(entry);
            sb.append("\n");
        }
        L.info("matches so far: \n{} ", sb.toString());
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
}
