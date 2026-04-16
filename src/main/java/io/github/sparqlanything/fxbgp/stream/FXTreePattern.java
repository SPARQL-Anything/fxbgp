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
    private int size;
    private Node graphPatternNode;
    private boolean isGraphPattern;

    private FXTreePattern(FXBGPAnnotation bpa, Node graphPatternNode) throws NotATreeException {
        this.graphPatternNode = graphPatternNode;
        isGraphPattern = true;
        init(bpa);
    }
    private FXTreePattern(FXBGPAnnotation bpa) throws NotATreeException {
        isGraphPattern = false;
        init(bpa);
    }

    private void init(FXBGPAnnotation bpa) throws NotATreeException {
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
        // TODO we don't check cycles or p_o s_o joins
        // Determine root first
        this.root = makeRoot(subjects.iterator().next(), bpa);
        this.variables = Collections.unmodifiableSet(variables);
        this.nodes = Collections.unmodifiableSet(nodes);
        //FXNode n = this.root;
        this.size = 1 + countChildren(this.root);
    }
    public int getSize(){
        return this.size;
    }

    private int countChildren(FXNode n){
        int soFar = n.getChildren().size();
        for(FXNode child : n.getChildren()){
            soFar += countChildren(child);
        }
        return soFar;
    }

    public static FXTreePattern make(FXBGPAnnotation bpa) throws NotATreeException {
        return new FXTreePattern(bpa);
    }

    public static FXTreePattern make(FXBGPAnnotation bpa, Node graphNode) throws NotATreeException {
        return new FXTreePattern(bpa, graphNode);
    }

    private static FXNode makeRoot(Node node, FXBGPAnnotation bpa) {
        for(Triple tq: bpa.getOpBGP().getPattern()){
            if(tq.getSubject().equals(node)){
                return makeNode(tq,null, node, bpa);
            }
        }
        throw new RuntimeException("Not a root?");
    }

    private static List<FXNode> makeChildren(Triple tp, Node parent, Node node, FXBGPAnnotation bpa) {
        List<FXNode> children = new ArrayList<>();
        for(Triple t : bpa.getOpBGP().getPattern()){
            if(t.getSubject().equals(node)) {
                children.add(makeNode(t, node, t.getPredicate(), bpa));
            }else if(t.getPredicate().equals(node) && t.getSubject().equals(parent) && tp.equals(t)) {
                children.add(makeNode(t, node, t.getObject(), bpa));
            }else if(t.getObject().equals(node)) {
                // Ignore, only subjects have children
            }
        }
        return Collections.unmodifiableList(children);
    }

    private static FXNode makeNode(Triple t, Node parent, Node node, FXBGPAnnotation bpa) {
        FXNodeAnnotation annotation = bpa.getAnnotation(node);
        List<FXNode> children = makeChildren(t, parent, node, bpa);
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

    public boolean isGraphPattern(){
        return isGraphPattern;
    }

    public Node getGraphPatternNode(){
        return this.graphPatternNode;
    }
}
