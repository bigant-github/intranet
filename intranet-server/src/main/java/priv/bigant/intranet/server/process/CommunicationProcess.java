package priv.bigant.intranet.server.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.*;
import priv.bigant.intrance.common.ServerConnector.ConnectorThread;
import priv.bigant.intranet.server.communication.ServerCommunication;
import priv.bigant.intranet.server.ServerConfig;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class CommunicationProcess extends ProcessBase {

    public static final Logger LOG = LoggerFactory.getLogger(CommunicationProcess.class);
    private ThreadPoolExecutor executor;

    public CommunicationProcess() {
        ServerConfig serverConfig = (ServerConfig) Config.getConfig();
        this.executor = new ThreadPoolExecutor(serverConfig.getCorePoolSize(), serverConfig.getMaximumPoolSize(), serverConfig.getKeepAliveTime(), TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
    }

    @Override
    public String getName() {
        return "CommunicationProcess";
    }

    @Override
    public void read(ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        ServerCommunication serverCommunication = (ServerCommunication) selectionKey.attachment();
        serverCommunication.disposeRequests();
    }

    @Override
    public void accept(ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
        socketChannel.configureBlocking(false);
        connectorThread.register(socketChannel, SelectionKey.OP_READ, new ServerCommunication(socketChannel));
    }


}
