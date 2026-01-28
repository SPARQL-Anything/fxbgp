package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.FX;
import io.github.sparqlanything.fxbgp.FXNodeAnnotation;
import org.apache.jena.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Matching_0 {
    private static final Logger L = LoggerFactory.getLogger(Matching_0.class);
    private Map<FXNode, Node> map;
    private Set<FXNode> cursor;
    private boolean unresolvable = false;
    private List<Node> contextPath = new ArrayList<>();
    // The first cursor is always the root of the pattern
    Matching_0(FXNode cursor, Node node) {
        if(cursor == null) throw new RuntimeException("cursor is null");
        if(!cursor.isRoot()) throw new RuntimeException("cursor is not root");
        this.cursor = new HashSet<>();
        this.cursor.add(cursor);
        this.map = new HashMap<>();
        this.map.put(cursor, node);
        this.contextPath.add(node);
//        L.info("Create new matching on cursor {} / node {}", cursor, node);
        //L.info("On node {}", node);
    }

    private Matching_0(Map<FXNode, Node> map, Set<FXNode> cursor, List<Node> contextPath) {
        this.map = map;
        this.cursor = cursor;
        this.contextPath = contextPath;
//        L.info("Spawn matching on cursor {} / path {} / size {}",
//                cursor, path, map.size());
    }

    Matching_0(FXNode cursor, Node node, List<Node> contextPath) {
        if(cursor == null) throw new RuntimeException("cursor is null");
        this.cursor = new HashSet<>();
        this.cursor.add(cursor);
        this.map = new HashMap<>();
        this.map.put(cursor, node);
        this.contextPath = contextPath;
//        L.info("Create new matching on cursor {} / node {}", cursor, node);
        //L.info("On node {}", node);
    }


    private void set(FXNode patternNode, Node sourceNode) {
        if (map.containsKey(patternNode)) {
            throw new RuntimeException("Duplicate matching");
        }else if(sourceNode.isLiteral() && sourceNode.getLiteral().getLexicalForm().equals("H1")) {
            //throw new RuntimeException("WTF!");
            System.out.println(sourceNode.getLiteralLexicalForm());
        }
        // Now we set the new matching node and we move the cursor
        map.put(patternNode, sourceNode);
    }

    public Map<FXNode, Node> getMap() {
        return Collections.unmodifiableMap(map);
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

    public void rollback(int steps) {
        Set<FXNode> next = new HashSet<>();
        for (FXNode c : this.cursor) {
            FXNode nc = null;
            for (int i = 0; i < steps; i++) {
                nc = c.getParent();
            }
            if(nc == null) {
                throw new RuntimeException("cursor is null");
            }
            next.add(nc);
        }
        this.cursor = next;
    }

    public void unset(FXNode patternNode) {
        this.map.remove(patternNode);
    }

    public Matching_0 copy(){
        return new Matching_0(new HashMap(this.map), new HashSet<>(this.cursor), new ArrayList<>(this.contextPath));
    }

    public Set<Matching_0> check(Node node, FX component) {
        L.trace("[start] check `{}` in path: {}", node, contextPath);
        if(component.equals(FX.Container)) {
            if(node.equals(contextPath.get(contextPath.size()-1))){
                throw new RuntimeException("this should not happen");
            }
            contextPath.add(node);
        }
        // Is the last node matching the cursor in the same scope of the last container in the path?
        if(!component.equals(FX.Container)) {
            FXNode scope;
            if(!cursor.iterator().hasNext()){
                throw new RuntimeException("This shall never happen!");
            }
            // If the cursor is a container, the scope is itself
            FXNode as = cursor.iterator().next();
            if(as == null){
                throw new RuntimeException("This shall never happen!");
            }
            FXNodeAnnotation ann = as.getAnnotation();
            if(ann.getTerm().equals(FX.Container)){
                scope = as;
            }else{
                // Otherwise, cursors must be on keys (one or more)
                // Thus, we are in the scope of their parent (which must be the same for all of them)
                scope = cursor.iterator().next().getParent();
            }
            // Is the node mapped to the container in scope the same as the last
            // container in the path?
            L.trace(" -- Scope node: {}", map.get(scope));
            L.trace(" -- Last node in path: {}", contextPath.get(contextPath.size()-1));
            if(!map.get(scope).equals(contextPath.get(contextPath.size()-1))){
                L.trace(" -- Off scope!");
                return Collections.emptySet(); // Don't do anything
            }
        }
        L.trace("In scope, proceed checking match ...");
        Set<Matching_0> spawned = new HashSet<>();
        // Is it matching (any) next node in the tree pattern?
        Set<FXNode> cursorsMatched = new HashSet<>();
        Set<FXNode> childrenFound = new HashSet<>();
        Set<FXNode> cursorsFailed =  new HashSet<>();
        for(FXNode c : this.cursor) {
            L.trace("check on cursor -- {} -- ", cursor);
            for(FXNode child: c.getChildren()) {
                // Is it matching the component type?
                if(child.getAnnotation().getTerm().equals(component)){
                    L.trace("term equals component -- {} -- ", component);
                    L.trace("node matching {} VS {} == {}" , child.getNode(), node, nodeMatches(child.getNode(), node));
                    // Do the incoming node matches the pattern node?
                    if(nodeMatches(child.getNode(), node)){
//                        L.info("On cursor {}", c);
//                        L.info("On node {} {}", node, component);
                        L.trace(" *** Matches child node {}", child.getNode());
                        childrenFound.add(child);
                        cursorsMatched.add(c);
                    }else {
                        // All is fine except the actual node ...
                        // We haven't found any children but the cursor was matched
                        cursorsFailed.add(c);
                    }
                }
            }
        }
        L.trace(" ...  finished checking match");
        // If the number of matches is less then the size of the cursor
        // We are having an asimmetric match
        // Remove the unmatched key
        if(!childrenFound.isEmpty() &&
                childrenFound.size() != cursor.size()){
            cursor.retainAll(cursorsMatched);
        }
        Set<FXNode> addToCursor = new HashSet<>();

        Set<FXNode> removeFromCursor = new HashSet<>();
        if(!cursorsFailed.isEmpty()){
            for(FXNode c : cursorsFailed){
                if(!c.isRoot()){
                    addToCursor.add(c.getParent());
                }else {
                    addToCursor.add(c);
                }
            }
        }

        // the node matches any of the children of the cursors
        for(FXNode child : childrenFound) {
            // Does the map already contains a match for this cursor child?
            if(map.containsKey(child)){
                L.trace("contains child: copying map {} ...", map);
                // If it does, spawn a new matching with the coming node
                Matching_0 copy = this.copy();
                copy.unset(child); // remove the last solution from the copy
                copy.set(child, node); // reset it with the coming node
                L.trace("into map {} ...", copy.getMap());
                spawned.add(copy);
            } else {
                // Otherwise, Before setting the match
                if (component.equals(FX.Value) ||
                        component.equals(FX.Type) ||
                        component.equals(FX.Container)){
                    L.trace("is object: copying map {} ...", map);
                    // If the cursor is a leaf, spawn a copy of this matching and rollback the
                    // cursor to the last container
                    Matching_0 copy = this.copy();
                    copy.unset(child.getParent());
                    //if(!component.equals(FX.Container)){
                    copy.rollback(1);
                    //}
                    L.trace("... into map {}", map);
                    spawned.add(copy);
                }
                this.set(child, node);
            }
            removeFromCursor.add(child.getParent()); // remove the cursor
            if(child.getAnnotation().getTerm().equals(FX.Value) ||
                    child.getAnnotation().getTerm().equals(FX.Type)||
                    child.getAnnotation().getTerm().equals(FX.Root)
            ){
                // Let's reset the cursor to the container
                FXNode cont = child.getParent().getParent();
                if(cont == null){
                    throw new RuntimeException("This should not happen");
                }
                addToCursor.add(cont);
            }else if(child.getAnnotation().getTerm().equals(FX.SlotString) ||
                    child.getAnnotation().getTerm().equals(FX.SlotNumber) ||
                    child.getAnnotation().getTerm().equals(FX.TypeProperty)
            ){
                // Let's set the cursor to the predicate node, waiting for the value/type/container
                addToCursor.add(child);
            }else if (child.getAnnotation().getTerm().equals(FX.Container)){
                // If it is a container, it can only be in the object position (because it is a child in the tree pattern)
                // We set the cursor on the child node, waiting for keys
                addToCursor.add(child);
            }else{
                throw new RuntimeException("Unexpected FX term");
            }
        }
        this.cursor.removeAll(removeFromCursor);
        this.cursor.addAll(addToCursor);
        L.trace("new cursor is: {} ", this.cursor);
        L.trace("[end] check {}. Now path is: {}", node, contextPath);
        return spawned;
    }

    public void endContainer(){
        // Check if the node is bound to any match.
        // If it is mark this as unresolvable
        if(map.values().contains(contextPath.get(contextPath.size()-1))){
            this.unresolvable = true;
        }
        // Remember the path
        contextPath.remove(contextPath.size()-1);
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
