package priv.bigant.intranet.client;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.Config;
import priv.bigant.intrance.common.Connector;
import priv.bigant.intrance.common.ProcessBase;
import priv.bigant.intrance.common.ServerConnector.ConnectorThread;
import priv.bigant.intrance.common.communication.Communication;
import priv.bigant.intrance.common.communication.CommunicationDispose;
import priv.bigant.intrance.common.communication.CommunicationRequest;
import priv.bigant.intrance.common.communication.CommunicationRequest.CommunicationRequestHttpReturn;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

public class CommunicationProcess extends ProcessBase {


    private static final Logger LOG = LoggerFactory.getLogger(CommunicationProcess.class);
    private ClientCommunication clientCommunication;
    private Connector connector;
    private CommunicationDispose communicationDispose;
    private ConnectorThread serviceConnector;

    public CommunicationProcess(ClientCommunication clientCommunication, ConnectorThread serviceConnector) {
        this.clientCommunication = clientCommunication;
        this.serviceConnector = serviceConnector;
        communicationDispose = new ClientCommunicationDispose(serviceConnector, this);
    }

    public void showdown() {
        connector.showdown();
        serviceConnector.showdown();
    }

    public Connector getConnector() {
        return connector;
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    @Override
    public void start() {

    }

    @Override
    public void read(ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        List<CommunicationRequest> communicationRequest = clientCommunication.readRequests();
        if (CollectionUtils.isNotEmpty(communicationRequest))
            communicationRequest.forEach(x -> communicationDispose.invoke(x, clientCommunication));
    }


    @Override
    public void accept(ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        //不可能有的
    }

    @Override
    public String getName() {
        return null;
    }

    public ConnectorThread getServiceConnector() {
        return serviceConnector;
    }

    public void setServiceConnector(ConnectorThread serviceConnector) {
        this.serviceConnector = serviceConnector;
    }

    /**
     * 统一处理请求
     */
    public static class ClientCommunicationDispose extends CommunicationDispose {
        private ClientConfig clientConfig;
        private static final Logger LOG = LoggerFactory.getLogger(ClientCommunicationDispose.class);
        private CommunicationProcess communicationProcess;
        private ConnectorThread serviceConnector;

        public ClientCommunicationDispose(ConnectorThread serviceConnector, CommunicationProcess communicationProcess) {
            this.serviceConnector = serviceConnector;
            this.communicationProcess = communicationProcess;
            clientConfig = (ClientConfig) Config.getConfig();
        }

        @Override
        protected void httpReturn(CommunicationRequest communicationRequest, Communication communication) {
            try {
                CommunicationRequestHttpReturn communicationRequestHttpReturn = communicationRequest.toJavaObject(CommunicationRequestHttpReturn.class);
                switch (communicationRequestHttpReturn.getStatus()) {
                    case SUCCESS:
                        LOG.info("连接成功");
                        break;
                    case DOMAIN_OCCUPIED:
                        communicationProcess.showdown();
                        LOG.error("域名已被占用");
                        break;
                }
            } catch (Exception e) {
                communicationProcess.showdown();
                LOG.error("连接失败", e);
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
            String id = communicationRequestHttpAdd.getId();
            try {
                socketChannel = SocketChannel.open(new InetSocketAddress(clientConfig.getHostName(), clientConfig.getHttpAcceptPort()));
                socketChannel.socket().setKeepAlive(true);
                Communication httpAddCommunication = new Communication(socketChannel);

                CommunicationRequest.CommunicationRequestHttpAdd communicationRequestHttpAdd1 = new CommunicationRequest.CommunicationRequestHttpAdd();
                communicationRequestHttpAdd1.setId(id);
                httpAddCommunication.writeN(CommunicationRequest.createCommunicationRequest(communicationRequestHttpAdd1));
                socketChannel.configureBlocking(false);
                serviceConnector.register(socketChannel, SelectionKey.OP_READ);
            } catch (Exception e) {
                LOG.error("add http socket error", e);
            }
        }
    }
}
