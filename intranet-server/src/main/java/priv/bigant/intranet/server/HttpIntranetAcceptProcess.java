package priv.bigant.intranet.server;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.*;
import priv.bigant.intrance.common.communication.CommunicationRequest;
import priv.bigant.intrance.common.communication.CommunicationRequest.CommunicationRequestHttpAdd;
import priv.bigant.intrance.common.communication.CommunicationResponse;
import priv.bigant.intrance.common.communication.HttpCommunication;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpIntranetAcceptProcess extends ProcessBase {

    public static final Logger LOG = LoggerFactory.getLogger(HttpIntranetAcceptProcess.class);
    private static final String NAME = "CommunicationProcess";
    private ThreadPoolExecutor executor;

    public HttpIntranetAcceptProcess() {
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
    public void read(ServerConnector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        selectionKey.cancel();

        ServerCommunication serverCommunication = (ServerCommunication) selectionKey.attachment();
        CommunicationRequest communicationRequest = serverCommunication.readRequest();
        if (communicationRequest == null)
            return;

        CommunicationRequestHttpAdd communicationRequestHttpAdd = communicationRequest.toJavaObject(CommunicationRequestHttpAdd.class);
        String id = communicationRequestHttpAdd.getId();
        SocketBean socketBean = new SocketBean(serverCommunication.getSocketChannel());
        socketBean.setId(id);

        HttpCommunication communication = HttpSocketManager.get(HttpSocketManager.getKey(id));
        communication.putSocketBean(socketBean);
    }

    @Override
    public void accept(ServerConnector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
        //executor.execute(new ReadProcessThread(socketChannel));

        ServerCommunication serverCommunication = new ServerCommunication(socketChannel);
        socketChannel.configureBlocking(false);
        connectorThread.register(socketChannel, SelectionKey.OP_READ, serverCommunication);

    }

    class ReadProcessThread implements Runnable {

        private SocketChannel socketChannel;
        private SocketBean socketBean;

        public ReadProcessThread(SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
            this.socketBean = new SocketBean(socketChannel);
        }


        @Override
        public void run() {
            try {
                ByteBuffer allocate = ByteBuffer.allocate(1024);
                int read = socketChannel.read(allocate);
                allocate.flip();
                CommunicationRequestHttpAdd communicationRequestHttpAdd = CommunicationResponse.createCommunicationResponse(ArrayUtils.subarray(allocate.array(), 0, read)).toJavaObject(CommunicationRequestHttpAdd.class);
                String id = communicationRequestHttpAdd.getId();
                socketBean.setId(id);
                HttpCommunication serverCommunication = HttpSocketManager.get(HttpSocketManager.getKey(id));
                serverCommunication.putSocketBean(socketBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*class ReadProcessThread implements Runnable {

        private SocketChannel socketChannel;
        private SocketBean socketBean;

        public ReadProcessThread(SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
            this.socketBean = new SocketBean(socketChannel);
        }



        @Override
        public void run() {
            try {
                ByteBuffer allocate = ByteBuffer.allocate(1024);
                int read = socketChannel.read(allocate);
                allocate.flip();
                CommunicationRequest.CommunicationRequestHttpAdd communicationRequestHttpAdd = CommunicationResponse.createCommunicationResponse(ArrayUtils.subarray(allocate.array(), 0, read)).toJavaObject(CommunicationRequest.CommunicationRequestHttpAdd.class);
                String id = communicationRequestHttpAdd.getId();
                socketBean.setId(id);
                HttpCommunication serverCommunication = HttpSocketManager.get(HttpSocketManager.getKey(id));
                serverCommunication.putSocketBean(socketBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/

}
