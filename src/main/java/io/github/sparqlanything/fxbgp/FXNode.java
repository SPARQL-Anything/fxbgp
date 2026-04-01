package io.github.sparqlanything.fxbgp;

public class FXNode {

    private final FX type;
    private final NodeGenerator nodeGenerator;

    public FXNode(FX type, NodeGenerator nodeGenerator) {
        this.type = type;
        this.nodeGenerator = nodeGenerator;
    }

    public FX getType() {
        return type;
    }

    public NodeGenerator getNodeGenerator() {
        return nodeGenerator;
    }
}
