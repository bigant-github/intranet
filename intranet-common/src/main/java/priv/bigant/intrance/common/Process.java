package priv.bigant.intrance.common;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface Process extends BigAnt {

    void start();

    void read(Connector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException;

    void accept(Connector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException;
}
