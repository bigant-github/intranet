package priv.bigant.intranet.client;

import priv.bigant.intrance.common.ProcessBase;
import priv.bigant.intrance.common.ServerConnector.ConnectorThread;
import priv.bigant.intrance.common.communication.Communication;
import priv.bigant.intrance.common.communication.CommunicationDispose;
import priv.bigant.intrance.common.communication.CommunicationRequest;
import priv.bigant.intrance.common.communication.CommunicationRequest.CommunicationRequestHttpReturn;
import priv.bigant.intrance.common.log.LogUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class CommunicationProcessor extends ProcessBase {


    private Communication clientCommunication;
    private ClientConfig clientConfig;


    public CommunicationProcessor(Communication clientCommunication, ConnectorThread serviceConnector) {
        this.clientCommunication = clientCommunication;
        //clientCommunication.setCommunicationDispose(new ClientCommunicationDispose(serviceConnector));
    }

    public CommunicationProcessor(Communication clientCommunication, ClientConfig clientConfig) {
        this.clientCommunication = clientCommunication;
        this.clientConfig = clientConfig;
    }

    public void showdown() {
        if (!clientCommunication.isClose()) {
            clientCommunication.close();
        }
    }


    @Override
    public void read(ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        clientCommunication.disposeRequest();
    }


    @Override
    public void accept(ConnectorThread connectorThread, SelectionKey selectionKey) {
        //不可能有的
    }

    @Override
    public String getName() {
        return "client communication process";
    }


    /**
     * 统一处理请求
     */
    public static class ClientCommunicationDispose extends CommunicationDispose {
        private ClientConfig clientConfig;
        private Logger log;
        private ConnectorThread serviceConnector;
        private Consumer<CommunicationRequestHttpReturn.Status> returnError;

        public ClientCommunicationDispose(ConnectorThread serviceConnector, ClientConfig clientConfig) {
            this.log = LogUtil.getLog(clientConfig.getLogName(), this.getClass());
            this.serviceConnector = serviceConnector;
            this.clientConfig = clientConfig;
        }

        public ClientCommunicationDispose(ConnectorThread serviceConnector, ClientConfig clientConfig, Consumer<CommunicationRequestHttpReturn.Status> returnError) {
            this(serviceConnector, clientConfig);
            this.returnError = returnError;
        }

        @Override
        protected void httpReturn(CommunicationRequest communicationRequest, Communication communication) {

            CommunicationRequestHttpReturn communicationRequestHttpReturn = communicationRequest.toJavaObject(CommunicationRequestHttpReturn.class);
            switch (communicationRequestHttpReturn.getStatus()) {
                case SUCCESS:
                    log.info("连接成功");
                    break;
                case DOMAIN_OCCUPIED:
                    log.severe("域名已被占用");
                    if (returnError != null) returnError.accept(CommunicationRequestHttpReturn.Status.DOMAIN_OCCUPIED);
            }

        }

        @Override
        protected void test(CommunicationRequest communicationRequest, Communication communication) {
            //测试数据先不管
        }

        @Override
        public void http(CommunicationRequest communicationRequest, Communication communication) {
            //客户端收不到此类型数据
        }

        @Override
        public void httpAdd(CommunicationRequest communicationRequest, Communication communication) {
            SocketChannel socketChannel;
            CommunicationRequest.CommunicationRequestHttpAdd communicationRequestHttpAdd = communicationRequest.toJavaObject(CommunicationRequest.CommunicationRequestHttpAdd.class);
            try {
                socketChannel = SocketChannel.open(new InetSocketAddress(clientConfig.getHostName(), clientConfig.getHttpAcceptPort()));
                socketChannel.socket().setKeepAlive(true);
                Communication.writeN(CommunicationRequest.createCommunicationRequest(communicationRequestHttpAdd), socketChannel, clientConfig);
                socketChannel.configureBlocking(false);
                serviceConnector.register(socketChannel, SelectionKey.OP_READ);
            } catch (Exception e) {
                log.severe("add http socket error" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
