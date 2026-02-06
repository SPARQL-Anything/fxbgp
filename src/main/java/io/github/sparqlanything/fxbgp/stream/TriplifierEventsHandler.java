package io.github.sparqlanything.fxbgp.stream;

public interface TriplifierEventsHandler {

    void onDataSource(String dataSourceId);

    void startRoot(String dataSourceId);

    void startContainer(String containerId);

    void onSlotNumber(int key);

    void onSlotString(String key);

    void onValue(Object obj);

    void onType(String type);

    void endContainer();

    void endRoot();
}
