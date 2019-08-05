package priv.bigant.intranet.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.*;
import priv.bigant.intrance.common.ServerConnector.ConnectorThread;
import priv.bigant.intrance.common.communication.*;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static priv.bigant.intrance.common.communication.CommunicationRequest.*;

public class HttpIntranetConnectorProcess extends ProcessBase {

    public static final Logger LOG = LoggerFactory.getLogger(HttpIntranetConnectorProcess.class);
    private static final String NAME = "HttpIntranetConnectorProcess";
    private ThreadPoolExecutor executor;

    public HttpIntranetConnectorProcess() {
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
    public void read(ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        selectionKey.interestOps(selectionKey.interestOps() & (~selectionKey.readyOps()));
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        executor.execute(new ReadProcessThread(socketChannel));
    }

    @Override
    public void accept(ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
        socketChannel.configureBlocking(false);
        connectorThread.register(socketChannel, SelectionKey.OP_READ);
    }

    class ReadProcessThread implements Runnable {
        final Logger LOG = LoggerFactory.getLogger(ReadProcessThread.class);
        private SocketChannel socketChannel;
        private ServerCommunication serverCommunication;
        private SocketBean socketBean;

        public ReadProcessThread(SocketChannel socketChannel) throws IOException {
            this.socketChannel = socketChannel;
            this.socketBean = new SocketBean(socketChannel);
        }

        @Override
        public void run() {
            CommunicationRequestHttpFirst communicationRequestHttpFirst = null;

            try {
                //创建server与客户端通信连接
                serverCommunication = new ServerCommunication(socketChannel);

                communicationRequestHttpFirst = serverCommunication.readRequest().toJavaObject(CommunicationRequestHttpFirst.class);

                //读取客户端配置信息
                String host = communicationRequestHttpFirst.getHost();
                serverCommunication.setHost(host);
                socketBean.setDomainName(host);
                boolean exist = HttpSocketManager.isExist(host);
                if (exist) {
                    boolean b = HttpSocketManager.get(host).isClose();
                    if (b) {//上一个连接已失效
                        HttpSocketManager.get(host).close();
                        HttpSocketManager.remove(host);
                    } else {//域名已存在
                        CommunicationRequestHttpReturn communicationRequestHttpReturn = new CommunicationRequestHttpReturn(CommunicationRequestHttpReturn.Status.DOMAIN_OCCUPIED);
                        serverCommunication.writeN(CommunicationRequest.createCommunicationRequest(communicationRequestHttpReturn));
                        serverCommunication.close();
                        LOG.info(host + CodeEnum.HOST_ALREADY_EXIST.getMsg());
                        return;
                    }
                }
                //连接成功
                HttpSocketManager.add(host, serverCommunication);
                CommunicationRequestHttpReturn communicationRequestHttpReturn = new CommunicationRequestHttpReturn(CommunicationRequestHttpReturn.Status.SUCCESS);
                serverCommunication.writeN(CommunicationRequest.createCommunicationRequest(communicationRequestHttpReturn));
                LOG.info(host + " 连接成功");
                for (int a = 0; a < 10; a++)
                    serverCommunication.createSocketBean();
            } catch (Exception e) {
                LOG.error("连接失败", e);
            }
        }
    }

}
