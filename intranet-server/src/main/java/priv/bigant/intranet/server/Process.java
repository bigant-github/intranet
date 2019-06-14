package priv.bigant.intranet.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public interface Process extends BigAnt {

    void start();

    void setConnector(Connector connector);

    Connector getConnector();

    void read(Connector.ConnectorThread connectorThread, SelectionKey socketChannel) throws IOException;

    void accept(Connector.ConnectorThread connectorThread, SelectionKey channel) throws IOException;
}
