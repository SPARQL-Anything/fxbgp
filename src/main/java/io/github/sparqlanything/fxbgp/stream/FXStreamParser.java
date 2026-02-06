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

    default Object get(FXEventType eventType) {
        switch (eventType) {
            case StartDataSource -> {
                return getDataSource();
            }
            case StartRoot ->  {
                return getRoot();
            }
            case StartContainer ->   {
                return getContainer();
            }
            case SlotNumber ->   {
                return getSlotNumber();
            }
            case SlotString ->  {
                return getSlotString();
            }
            case Value ->   {
                return getValue();
            }
            case Type ->   {
                return getType();
            }
        }

        throw new RuntimeException("unsupported event type");
    }
}
