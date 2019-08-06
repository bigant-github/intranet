package priv.bigant.intranet.server;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.*;
import priv.bigant.intrance.common.ServerConnector.ConnectorThread;
import priv.bigant.intrance.common.communication.*;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static priv.bigant.intrance.common.communication.CommunicationRequest.*;

public class CommunicationProcess extends ProcessBase {

    public static final Logger LOG = LoggerFactory.getLogger(CommunicationProcess.class);
    private static final String NAME = "CommunicationProcess";
    private ThreadPoolExecutor executor;
    private ServerCommunicationDispose serverCommunicationDispose;

    public CommunicationProcess() {
        ServerConfig serverConfig = (ServerConfig) Config.getConfig();
        this.serverCommunicationDispose = new ServerCommunicationDispose();
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
        //selectionKey.interestOps(selectionKey.interestOps() & (~selectionKey.readyOps()));
        ServerCommunication serverCommunication = (ServerCommunication) selectionKey.attachment();
        List<CommunicationRequest> communicationRequests = serverCommunication.readRequests();
        if (CollectionUtils.isNotEmpty(communicationRequests))
            communicationRequests.forEach(x -> serverCommunicationDispose.invoke(x, serverCommunication));
        //executor.execute(new ReadProcessThread(serverCommunication));
    }

    @Override
    public void accept(ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
        socketChannel.configureBlocking(false);
        connectorThread.register(socketChannel, SelectionKey.OP_READ, new ServerCommunication(socketChannel));
    }

    /**
     * 统一处理请求
     */
    public static class ServerCommunicationDispose extends CommunicationDispose {

        @Override
        protected void httpReturn(CommunicationRequest communicationRequest, Communication communication) {
            //服务端没有这个类型请求
        }

        @Override
        protected void test(CommunicationRequest communicationRequest, Communication communication) {
            //test数据暂时不管
        }

        @Override
        protected void http(CommunicationRequest communicationRequest, Communication communication) {
            new ReadProcessThread((ServerCommunication) communication, communicationRequest).run();
        }

        @Override
        protected void httpAdd(CommunicationRequest communicationRequest, Communication communication) {
            //暂不处理客户端此类请求
        }
    }

    static class ReadProcessThread implements Runnable {
        final Logger LOG = LoggerFactory.getLogger(ReadProcessThread.class);
        private ServerCommunication serverCommunication;
        private CommunicationRequestHttpFirst communicationRequestHttpFirst;

        public ReadProcessThread(ServerCommunication serverCommunication, CommunicationRequest communicationRequest) {
            this.serverCommunication = serverCommunication;
            this.communicationRequestHttpFirst = communicationRequest.toJavaObject(CommunicationRequestHttpFirst.class);
        }

        @Override
        public void run() {

            try {
                //读取客户端配置信息
                String host = communicationRequestHttpFirst.getHost();
                serverCommunication.setHost(host);
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
