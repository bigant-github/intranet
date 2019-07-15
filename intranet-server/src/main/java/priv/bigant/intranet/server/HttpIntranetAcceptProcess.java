package priv.bigant.intranet.server;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.Connector;
import priv.bigant.intrance.common.ProcessBase;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.communication.CommunicationRequest;
import priv.bigant.intrance.common.communication.CommunicationResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Stack;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpIntranetAcceptProcess extends ProcessBase {

    public static final Logger LOG = LoggerFactory.getLogger(HttpIntranetAcceptProcess.class);
    private static final String NAME = "HttpIntranetConnectorProcess";
    private Stack stack;
    private ThreadPoolExecutor executor;
    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

    public HttpIntranetAcceptProcess() {
        stack = new Stack<>();
        ServerConfig serverConfig = (ServerConfig) Config.getConfig();
        this.executor = new ThreadPoolExecutor(serverConfig.getCorePoolSize(), serverConfig.getMaximumPoolSize(), serverConfig.getKeepAliveTime(), TimeUnit.MILLISECONDS, new SynchronousQueue<Runnable>());
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void read(Connector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
    }

    @Override
    public void accept(Connector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
        executor.execute(new ReadProcessThread(socketChannel));
    }

    class ReadProcessThread implements Runnable {

        private SocketChannel socketChannel;
        private ServerCommunication serverCommunication;
        private SocketBean socketBean;

        public ReadProcessThread(SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
            this.socketBean = new SocketBean(socketChannel);
        }

        @Override
        public void run() {
            try {
                byteBuffer.clear();
                int read = socketChannel.read(byteBuffer);
                byteBuffer.flip();
                CommunicationRequest.CommunicationRequestHttpAdd communicationRequestHttpAdd = CommunicationResponse.createCommunicationResponse(ArrayUtils.subarray(byteBuffer.array(), 0, read)).toJavaObject(CommunicationRequest.CommunicationRequestHttpAdd.class);
                String id = communicationRequestHttpAdd.getId();
                socketBean.setId(id);
                ServerCommunication serverCommunication = HttpSocketManager.get(HttpSocketManager.getKey(id));
                serverCommunication.putSocketBean(socketBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
