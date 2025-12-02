package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.jdbc.QueryComponentFactory;
import org.apache.jena.graph.Node;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Matching {
    private Map<FXNode, Node> map;
    private Set<FXNode> cursor;
    private boolean unresolvable = false;
    // The first cursor is always the root of the pattern
    Matching(FXNode cursor, Node node) {
        this.cursor = new HashSet<>();
        this.cursor.add(cursor);
        this.map = new HashMap<>();
        this.map.put(cursor, node);
    }

    private Matching(Map<FXNode, Node> map, Set<FXNode> cursor) {
        this.map = map;
        this.cursor = cursor;
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
        return new Matching(this.map, this.cursor);
    }

    public Set<Matching> check(Node node, FX component) {
        Set<Matching> spawned = new HashSet<>();
        // Is it matching (any) next node in the tree pattern?
        Set<FXNode> childrenFound = new HashSet<>();
        for(FXNode c : this.cursor) {
            for(FXNode child: c.getChildren()) {
                // Is it matching the component type?
                if(child.getAnnotation().getTerm().equals(component)){
                    // Do the incoming node matches the pattern node?
                    if(nodeMatches(child.getNode(), node)){
                        childrenFound.add(child);
                        // Does the map already contains a match?
                        if(map.containsKey(child)){
                            Matching copy = this.copy();
                            spawned.add(copy);
                            copy.unset(child);
                            copy.set(child, node);
                        }else{
                            this.set(child, node);
                        }
                    }
                }
            }
        }
        for(FXNode c : childrenFound) {
            this.cursor.remove(c.getParent());
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
                // Let's reset the cursor to the container
                this.cursor.add(c);
            }else if (c.getAnnotation().getTerm().equals(FX.Container)){
                // If it is a container, it can only be in the object position (because it is a children in the tree pattern)
                this.cursor.add(c);
            }else{
                throw new RuntimeException("Unexpected FX term");
            }
        }
        return spawned;
    }

    public void endContainer(){
        // What happens to these matches if a container is closed?
        // Look at cursors
        for(FXNode c : this.cursor) {
            // if the cursor is a container, rollback to the parent container
            if(c.getAnnotation().getTerm().equals(FX.Container)){
                if(!c.isRoot()) {
                    rollback(2);
                }else{
                    // If it is the root node, and the map is incomplete, there is no solution
                    // (if the matches where completed, they would have been removed)
                    // mark this as unresolvable
                    this.unresolvable = true;
                    return;
                }
            }else if(c.getAnnotation().getTerm().equals(FX.SlotNumber)
            || c.getAnnotation().getTerm().equals(FX.TypeProperty) ||
                    c.getAnnotation().getTerm().equals(FX.SlotString) ){
                this.unresolvable = true;
                return;
            }
            // I would assume there is either a single container or a set of properties here
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
}
