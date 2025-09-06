package io.github.sparqlanything.fxbgp.stream;

public interface FXParserEventListener {

    public void startContainer(String localId);

    public void onIntegerKey(int localId);

    public void onStringKey(String localId);

    public void onValue(Object value);

    public void onType(String localId);

    public void endContainer();
}
