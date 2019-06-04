package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.communication.CodeEnum;
import priv.bigant.intrance.common.communication.CommunicationRequest;
import priv.bigant.intrance.common.communication.CommunicationResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Stack;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpIntranetConnectorProcess extends ProcessBase {

    public static final Logger LOG = LoggerFactory.getLogger(HttpIntranetConnectorProcess.class);
    private static final String NAME = "HttpIntranetConnectorProcess";
    private Stack stack;
    private ThreadPoolExecutor executor;

    public HttpIntranetConnectorProcess() {
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
    public void read(Connector.ConnectorThread connectorThread, SocketChannel socketChannel) {
    }

    @Override
    public void accept(Connector.ConnectorThread connectorThread, SocketChannel channel) throws IOException {
        //connectorThread.register(channel, SelectionKey.OP_READ);
        executor.execute(new ReadProcessThread(channel));
    }

    class ReadProcessThread implements Runnable {

        private SocketChannel socketChannel;
        private ServerCommunication serverCommunication;
        private SocketBean socketBean;

        private ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        public ReadProcessThread(SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
            this.socketBean = new SocketBean(socketChannel);
        }

        @Override
        public void run() {
            try {
                //创建server与客户端通信连接
                serverCommunication = new ServerCommunication(socketChannel);
                //读取客户端配置信息
                CommunicationRequest.CommunicationRequestHttpFirst communicationRequestHttpFirst = serverCommunication.readRequest().toJavaObject(CommunicationRequest.CommunicationRequestHttpFirst.class);

                String host = communicationRequestHttpFirst.getHost();
                serverCommunication.setHost(host);
                socketBean.setDomainName(host);
                boolean exist = HttpSocketManager.isExist(host);
                if (exist) {
                    boolean b = HttpSocketManager.get(host).isClose();
                    if (b) {
                        HttpSocketManager.get(host).close();
                        HttpSocketManager.remove(host);
                    } else {
                        serverCommunication.writeN(CommunicationResponse.create(CodeEnum.HOST_ALREADY_EXIST));
                        serverCommunication.close();
                        LOG.info(host + CodeEnum.HOST_ALREADY_EXIST.getMsg());
                        return;
                    }
                }
                HttpSocketManager.add(host, serverCommunication);
                serverCommunication.writeN(CommunicationResponse.createSuccess());
                LOG.info(host + " 连接成功");
                for (int a = 0; a < 10; a++)
                    serverCommunication.createSocketBean();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
