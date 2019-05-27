package priv.bigant.intranet.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class ProcessBase implements Process {

    private Connector connector;

    @Override
    public Connector getConnector() {
        return connector;
    }

    @Override
    public void setConnector(Connector connector) {
        this.connector = connector;
    }
}
