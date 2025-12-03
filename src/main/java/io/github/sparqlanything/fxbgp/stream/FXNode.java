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

    public FXNode goToRoot(){
        FXNode r = this;
        while(!r.isRoot())
            r = r.getParent();
        return r;
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

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(getNode().toString());
        sb.append(" [");
        sb.append(getAnnotation().toString());
        sb.append("] {" );
        for(FXNode child : children){
            sb.append(child.toString());
        }
        sb.append("}" );
        return sb.toString();
    }
}
