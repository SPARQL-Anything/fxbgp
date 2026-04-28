package io.github.sparqlanything.fxbgp;

public class FXNodeGenerator {

    private final FX type;
    private final NodeGenerator nodeGenerator;

    public FXNodeGenerator(FX type, NodeGenerator nodeGenerator) {
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
