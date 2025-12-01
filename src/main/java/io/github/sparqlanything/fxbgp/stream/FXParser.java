package io.github.sparqlanything.fxbgp.stream;

import java.io.IOException;

public abstract class FXParser {
    protected FXNodeEventListener eventListener;
    public FXParser(FXNodeEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public abstract void parse() throws IOException;
}
