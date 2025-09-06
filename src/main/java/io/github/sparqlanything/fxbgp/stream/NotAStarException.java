package io.github.sparqlanything.fxbgp.stream;

import org.apache.jena.sparql.core.BasicPattern;

public class NotAStarException extends Exception {
    private final BasicPattern bp;

    public NotAStarException(BasicPattern bp, String msg) {
        super(msg);
        this.bp = bp;
    }
    public NotAStarException(BasicPattern bp) {
        this.bp=bp;
    }
    public BasicPattern getBP() {
        return this.bp;
    }
}
