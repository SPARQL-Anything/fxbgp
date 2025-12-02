package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import io.github.sparqlanything.fxbgp.FXNodeAnnotation;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

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
    private FXTreePattern(FXBGPAnnotation bpa) {
        // Assumption: is a star pattern
        // Verify it is a star pattern
        // Subjects must all be joined
        Node root = null;
        Set<Node> nodes = new HashSet<>();
        Set<Node> variables = new HashSet<>();
        for(Triple t : bpa.getOpBGP().getPattern()){
            if(root == null) {
                root = t.getSubject();
            }
//            else if(!root.equals(t.getSubject())) {
//                throw new NotAStarException(bpa.getOpBGP().getPattern());
//            }
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
        this.root = makeRoot(root, bpa);
        this.variables = Collections.unmodifiableSet(variables);
        this.nodes = Collections.unmodifiableSet(nodes);
    }

    public static FXTreePattern make(FXBGPAnnotation bpa) {
        return new FXTreePattern(bpa);
    }

    private static FXNode makeRoot(Node node, FXBGPAnnotation bpa) {
        return makeNode(null, node, bpa);
    }

    private static List<FXNode> makeChildren(Node node, FXBGPAnnotation bpa) {
        List<FXNode> children = new ArrayList<>();
        for(Triple t : bpa.getOpBGP().getPattern()){
            if(t.getSubject().equals(node)) {
                children.add(makeNode(node, t.getPredicate(), bpa));
            }else if(t.getPredicate().equals(node)) {
                children.add(makeNode(node, t.getObject(), bpa));
            }else if(t.getObject().equals(node)) {
                // Ignore
            }
        }
        return Collections.unmodifiableList(children);
    }

    private static FXNode makeNode(Node parent, Node node, FXBGPAnnotation bpa) {
        FXNodeAnnotation annotation = bpa.getAnnotation(node);
        List<FXNode> children = makeChildren(node, bpa);
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
}
