package priv.bigant.intrance.common;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public interface Process extends BigAnt {

    void start();

    void read(ServerConnector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException;

    void accept(ServerConnector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException;
}
