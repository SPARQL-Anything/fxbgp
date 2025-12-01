package io.github.sparqlanything.fxbgp.stream;

import io.github.sparqlanything.fxbgp.FXNodeAnnotation;
import org.apache.jena.graph.Node;

import java.util.List;

public class FXNode {
    private Node node;
    private FXNode parent;
    private List<FXNode> children;
    private FXNodeAnnotation annotation;

    FXNode(Node node, FXNodeAnnotation annotation, List<FXNode> children) {
        this.node = node;
        this.annotation = annotation;
        this.children = children;
        for (FXNode child : children) {
            child.parent = this;
        }
    }

    public FXNodeAnnotation getAnnotation() {
        return annotation;
    }

    public boolean isRoot() {
        return parent == null;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public Node getNode() {
        return node;
    }

    public FXNode getParent() {
        return parent;
    }

    public List<FXNode> getChildren() {
        return children;
    }
}
