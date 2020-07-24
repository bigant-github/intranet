package priv.bigant.intrance.common;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * Nio 处理中心
 */
public interface Process extends BigAnt {

    void showdown();

    void read(ServerConnector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException;

    void accept(ServerConnector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException;
}
