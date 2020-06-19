package priv.bigant.intranet.server.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.*;
import priv.bigant.intrance.common.communication.Communication;
import priv.bigant.intrance.common.communication.CommunicationRequest;
import priv.bigant.intrance.common.communication.CommunicationRequest.CommunicationRequestHttpAdd;
import priv.bigant.intrance.common.communication.HttpCommunication;
import priv.bigant.intranet.server.ServerConfig;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 客户端与服务端进行http信息交换器
 */
public class HttpIntranetAcceptProcess extends ProcessBase {

    public static final Logger LOG = LoggerFactory.getLogger(HttpIntranetAcceptProcess.class);
    private ThreadPoolExecutor executor;

    public HttpIntranetAcceptProcess() {
        ServerConfig serverConfig = (ServerConfig) Config.getConfig();
        this.executor = new ThreadPoolExecutor(serverConfig.getCorePoolSize(), serverConfig.getMaximumPoolSize(), serverConfig.getKeepAliveTime(), TimeUnit.MILLISECONDS, new SynchronousQueue<>());
    }

    public HttpIntranetAcceptProcess(int corePoolSize, int maximumPoolSize, int keepAliveTime) {
        ServerConfig serverConfig = (ServerConfig) Config.getConfig();
        this.executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new SynchronousQueue<>());
    }


    @Override
    public String getName() {
        return "HttpIntranetAcceptProcess";
    }

    @Override
    public void read(ServerConnector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        selectionKey.cancel();

        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        CommunicationRequest communicationRequest = Communication.readRequest(socketChannel);

        if (communicationRequest == null)
            return;

        CommunicationRequestHttpAdd communicationRequestHttpAdd = communicationRequest.toJavaObject(CommunicationRequestHttpAdd.class);

        SocketBean socketBean = new SocketBean(socketChannel, communicationRequestHttpAdd.getId());

        HttpCommunication communication = HttpSocketManager.get(HttpSocketManager.getKey(communicationRequestHttpAdd.getId()));
        communication.putSocketBean(socketBean);
    }

    @Override
    public void accept(ServerConnector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
        socketChannel.configureBlocking(false);
        connectorThread.register(socketChannel, SelectionKey.OP_READ);
    }

}
