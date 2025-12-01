package io.github.sparqlanything.fxbgp.stream;

public interface TriplifierEventsHandler {
    void startRoot(String dataSourceId);

    void startContainer(String containerId);

    void onSlotNumber(int key);

    void onSlotString(String key);

    void onValue(Object obj);

    void endContainer();

    void endRoot();
}
