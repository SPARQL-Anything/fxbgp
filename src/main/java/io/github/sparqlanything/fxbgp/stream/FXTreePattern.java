package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXNodeAnnotation;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Note: Only Star Patterns so far
 */
public class FXTreePattern {
    private Set<Node> nodes;
    private FXNode root;
    private Set<Node> variables;
    private FXTreePattern(FXBGPAnnotation bpa) throws NotATreeException {
        // Verify it is a tree pattern
        // All but one subject must be objects as well
        Set<Node> nodes = new HashSet<>();
        Set<Node> variables = new HashSet<>();
        Set<Node> subjects = new HashSet<>();
//        Set<Node> predicates = new HashSet<>();
        Set<Node> objects = new HashSet<>();
        for(Triple t : bpa.getOpBGP().getPattern()){
            subjects.add(t.getSubject());
            objects.add(t.getObject());
            if(t.getSubject().isVariable()){
                variables.add(t.getSubject());
            }
            if(t.getPredicate().isVariable()){
                variables.add(t.getPredicate());
            }
            if(t.getObject().isVariable()){
                variables.add(t.getObject());
            }
            nodes.add(t.getSubject());
            nodes.add(t.getPredicate());
            nodes.add(t.getObject());
        }

        // Root candidates
        subjects.removeAll(objects);
        if(subjects.size() != 1){
            throw new NotATreeException(bpa.getOpBGP().getPattern());
        }
        // Determine root first
        this.root = makeRoot(subjects.iterator().next(), bpa);
        this.variables = Collections.unmodifiableSet(variables);
        this.nodes = Collections.unmodifiableSet(nodes);
    }

    public static FXTreePattern make(FXBGPAnnotation bpa) throws NotATreeException {
        return new FXTreePattern(bpa);
    }

    private static FXNode makeRoot(Node node, FXBGPAnnotation bpa) {
        return makeNode(null, node, bpa);
    }

    private static List<FXNode> makeChildren(Node parent, Node node, FXBGPAnnotation bpa) {
        List<FXNode> children = new ArrayList<>();
        for(Triple t : bpa.getOpBGP().getPattern()){
            if(t.getSubject().equals(node)) {
                children.add(makeNode(node, t.getPredicate(), bpa));
            }else if(t.getPredicate().equals(node) && t.getSubject().equals(parent)) {
                children.add(makeNode(node, t.getObject(), bpa));
            }else if(t.getObject().equals(node)) {
                // Ignore, only subjects have children
            }
        }
        return Collections.unmodifiableList(children);
    }

    private static FXNode makeNode(Node parent, Node node, FXBGPAnnotation bpa) {
        FXNodeAnnotation annotation = bpa.getAnnotation(node);
        List<FXNode> children = makeChildren(parent, node, bpa);
        return new FXNode(node,  annotation, children);
    }

    public FXNode getRoot(){
        return this.root;
    }

    public Set<Node> variables(){
        return variables;
    }
    public Set<Node> nodes(){
        return nodes;
    }

    public String toString(){
        return this.root.toString();
    }
}
