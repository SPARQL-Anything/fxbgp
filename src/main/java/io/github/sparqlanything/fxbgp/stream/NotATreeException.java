package io.github.sparqlanything.fxbgp.stream;

import org.apache.jena.sparql.core.BasicPattern;

public class NotATreeException extends Exception {
    private final BasicPattern bp;

    public NotATreeException(BasicPattern bp, String msg) {
        super(msg);
        this.bp = bp;
    }
    public NotATreeException(BasicPattern bp) {
        this.bp=bp;
    }
    public BasicPattern getBP() {
        return this.bp;
    }
}
