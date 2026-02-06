package io.github.sparqlanything.fxbgp.stream;

import java.util.Iterator;

public interface FXStreamParser extends Iterator<FXEventType> {

    default boolean hasNext(){
        return hasNextEvent();
    }

    boolean hasNextEvent();

    FXEventType nextType();

    default FXEventType next(){
        return nextType();
    }

    Object get(FXEventType eventType);

    Object getValue();

    String getContainer();

    String getDataSource();

    int getSlotNumber();

    String getSlotString();

    String getRoot();

    String getType();

    boolean isCompleted();

    boolean isCancelled();

    void cancel();
}
