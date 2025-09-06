package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.FXAnnotation;
import io.github.sparqlanything.fxbgp.FXBGPAnnotation;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FXTreePattern {
    private FXNode root;

    private FXTreePattern(FXNode root) {
        this.root = root;
    }

    public static FXTreePattern make(FXBGPAnnotation bpa) throws NotAStarException {
        // Assumption: is a star pattern
        // Verify it is a star pattern
        // Subjects must all be joined
        Node root = null;
        for(Triple t : bpa.getOpBGP().getPattern()){
            if(root == null) {
                root = t.getSubject();
            } else if(!root.equals(t.getSubject())) {
                throw new NotAStarException(bpa.getOpBGP().getPattern());
            }
        }
        // Build tree
        FXNode fxroot = makeRoot(root, bpa);
        FXTreePattern tp = new FXTreePattern(fxroot);
        return tp;
    }

    private static FXNode makeRoot(Node node, FXBGPAnnotation bpa) throws NotAStarException {
        return makeNode(null, node, bpa);
    }

    private static List<FXNode> makeChildren(Node node, FXBGPAnnotation bpa) throws NotAStarException {
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

    private static FXNode makeNode(Node parent, Node node, FXBGPAnnotation bpa) throws NotAStarException {
        FXAnnotation annotation = bpa.getAnnotation(node);
        List<FXNode> children = makeChildren(node, bpa);
        return new FXNode(node,  annotation, children);
    }

    public FXNode getRoot(){
        return this.root;
    }

    public static class FXNode {
        private Node node;
        private FXNode parent;
        private List<FXNode> children;
        private FXAnnotation annotation;
        FXNode(Node node, FXAnnotation annotation, List<FXNode> children) {
            this.node = node;
            this.annotation = annotation;
            this.children = children;
            for(FXNode child : children){
                child.parent = this;
            }
        }
        protected void setAnnotation(FXAnnotation annotation) {}
        public boolean isRoot(){
            return parent == null;
        }
        public boolean isLeaf(){
            return children.isEmpty();
        }
        public Node getNode(){
            return node;
        }
        public FXNode getParent(){
            return parent;
        }
        public List<FXNode> getChildren(){
            return children;
        }
    }
}
