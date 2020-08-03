package priv.bigant.intranet.server.process;

import priv.bigant.intrance.common.HttpSocketManager;
import priv.bigant.intrance.common.ProcessBase;
import priv.bigant.intrance.common.ServerConnector;
import priv.bigant.intrance.common.SocketBean;
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
public class IntranetProcessor extends ProcessBase {

    private ThreadPoolExecutor executor;
    private ServerConfig serverConfig;

    public IntranetProcessor(ServerConfig config) {
        this.serverConfig = config;
        this.executor = new ThreadPoolExecutor(serverConfig.getCorePoolSize(), serverConfig.getMaximumPoolSize(), serverConfig.getKeepAliveTime(), TimeUnit.MILLISECONDS, new SynchronousQueue<>());
    }


    @Override
    public String getName() {
        return "HttpIntranetAcceptProcess";
    }

    @Override
    public void showdown() {

    }

    @Override
    public void read(ServerConnector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        selectionKey.cancel();

        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        CommunicationRequest communicationRequest = Communication.readRequest(socketChannel, serverConfig.getLogName());

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
