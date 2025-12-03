package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.jdbc.QueryComponentFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;
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
    private Map<FXNode, Node> map;
    private Set<FXNode> cursor;
    private boolean unresolvable = false;
    private List<Node> path = new ArrayList<>();
    // The first cursor is always the root of the pattern
    Matching(FXNode cursor, Node node) {
        this.cursor = new HashSet<>();
        this.cursor.add(cursor);
        this.map = new HashMap<>();
        this.map.put(cursor, node);
        this.path.add(node);
//        L.info("Create new matching on cursor {} / node {}", cursor, node);
        //L.info("On node {}", node);
    }

    private Matching(Map<FXNode, Node> map, Set<FXNode> cursor, List<Node> path) {
        this.map = map;
        this.cursor = cursor;
        this.path = path;
//        L.info("Spawn matching on cursor {} / path {} / size {}",
//                cursor, path, map.size());
    }
    Matching(FXNode cursor, Node node, List<Node> path) {
        this.cursor = new HashSet<>();
        this.cursor.add(cursor);
        this.map = new HashMap<>();
        this.map.put(cursor, node);
        this.path = path;
//        L.info("Create new matching on cursor {} / node {}", cursor, node);
        //L.info("On node {}", node);
    }


    private void set(FXNode patternNode, Node sourceNode) {
        if (map.containsKey(patternNode)) {
            throw new RuntimeException("Duplicate matching");
        }
        // Now we set the new matching node and we move the cursor
        map.put(patternNode, sourceNode);
    }

    public Map<FXNode, Node> getMap() {
        return Collections.unmodifiableMap(map);
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

    public void rollback(int steps) {
        Set<FXNode> next = new HashSet<>();
        for (FXNode c : this.cursor) {
            for (int i = 0; i < steps; i++) {
                c = c.getParent();
            }
            next.add(c);
        }
        this.cursor = next;
    }

    public void unset(FXNode patternNode) {
        this.map.remove(patternNode);
    }

    public Matching copy(){
        return new Matching(new HashMap(this.map), new HashSet<>(this.cursor), new ArrayList<>(this.path));
    }

    public Set<Matching> check(Node node, FX component) {
        if(component.equals(FX.Container)) {
            path.add(node);
        }
        // Is the last node matching the cursor in the same scope of the last container in the path?
        if(!component.equals(FX.Container)) {
            FXNode scope;
            // If the cursor is a container, the scope is itself
            if(cursor.iterator().next().getAnnotation().getTerm().equals(FX.Container)){
                scope = cursor.iterator().next();
            }else{
                // Otherwise, cursors must be on keys (one or more)
                // Thus, we are in the scope of their parent (which must be the same for all of them)
                scope = cursor.iterator().next().getParent();
            }
            // Is the node mapped to the container in scope the same as the last
            // container in the path?
            if(!map.get(scope).equals(path.get(path.size()-1))){
                return Collections.emptySet(); // Don't do anything
            }
        }

        Set<Matching> spawned = new HashSet<>();
        // Is it matching (any) next node in the tree pattern?
        Set<FXNode> childrenFound = new HashSet<>();
//        L.info("proceed -- {} -- {} ({})", cursor.iterator().next().goToRoot().toString(), node, component);
        for(FXNode c : this.cursor) {
            for(FXNode child: c.getChildren()) {
                // Is it matching the component type?
                if(child.getAnnotation().getTerm().equals(component)){
                    // Do the incoming node matches the pattern node?
                    if(nodeMatches(child.getNode(), node)){
//                        L.info("On cursor {}", c);
//                        L.info("On node {} {}", node, component);
//                        L.info("Matches child node {}", child.getNode());
                        childrenFound.add(child);
                        // Does the map already contains a match for this cursor child?
                        if(map.containsKey(child)){
                            // If it does, spawn a new matching with the coming node
                            Matching copy = this.copy();
                            copy.unset(child); // remove the last solution from the copy
                            copy.set(child, node); // reset it with the coming node
                            spawned.add(copy);
                        } else {
                            // Otherwise, Before setting the match
                            if (component.equals(FX.Value) ||
                                    component.equals(FX.Type) ||
                                    component.equals(FX.Container)){
                                // If the cursor is a leaf, spawn a copy of this matching and rollback the
                                // cursor to the last container
                                Matching copy = this.copy();
                                copy.unset(c); // Remove the key from the cursor
                                copy.rollback(1);
                                spawned.add(copy);
                            }
                            this.set(child, node);
                        }
                    }
                }
            }
        }

        // the node matches any of the children of the cursors
        for(FXNode c : childrenFound) {
            this.cursor.remove(c.getParent()); // remove the cursor
            if(c.getAnnotation().getTerm().equals(FX.Value) ||
                    c.getAnnotation().getTerm().equals(FX.Type)||
                    c.getAnnotation().getTerm().equals(FX.Root)
            ){
                // Let's reset the cursor to the container
                this.cursor.add(c.getParent().getParent());
            }else if(c.getAnnotation().getTerm().equals(FX.SlotString) ||
                    c.getAnnotation().getTerm().equals(FX.SlotNumber) ||
                    c.getAnnotation().getTerm().equals(FX.TypeProperty)
            ){
                // Let's set the cursor to the predicate node, waiting for the value/type/container
                this.cursor.add(c);
            }else if (c.getAnnotation().getTerm().equals(FX.Container)){
                // If it is a container, it can only be in the object position (because it is a child in the tree pattern)
                // We set the cursor on the child node, waiting for keys
                this.cursor.add(c);
            }else{
                throw new RuntimeException("Unexpected FX term");
            }
        }
        return spawned;
    }

    public void endContainer(){
        // Check if the node is bound to any match.
        // If it is mark this as unresolvable
        if(map.values().contains(path.get(path.size()-1))){
            this.unresolvable = true;
        }
        // Remember the path
        path.remove(path.size()-1);
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
