package priv.bigant.intranet.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.ChannelStream;
import priv.bigant.intrance.common.ServerConnector.ConnectorThread;
import priv.bigant.intrance.common.communication.Communication;
import priv.bigant.intrance.common.communication.CommunicationEnum;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import static priv.bigant.intrance.common.communication.CommunicationRequest.CommunicationRequestHttpFirst;
import static priv.bigant.intrance.common.communication.CommunicationRequest.createCommunicationRequest;

/**
 * @author GaoLei 保持客户端与服务端通信
 */
public class ClientCommunication extends Communication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCommunication.class);
    private ClientConfig clientConfig;
    /**
     * http 交互 监控线程
     */
    private ConnectorThread serviceConnectorThread;
    /**
     * 客户端与服务端交互 监控线程
     */
    private ConnectorThread connectorThread;

    public ClientCommunication(ConnectorThread serviceConnectorThread, ClientConfig clientConfig) {
        this.serviceConnectorThread = serviceConnectorThread;
        this.clientConfig = clientConfig;
    }


    /**
     * 与服务器进行连接
     *
     * @throws Exception
     */
    public void connect() throws Exception {
        this.socketChannel = SocketChannel.open(new InetSocketAddress(clientConfig.getHostName(), clientConfig.getIntranetPort()));
        this.channelStream = new ChannelStream(socketChannel, 1024);

        socketChannel.socket().setKeepAlive(true);
        socketChannel.socket().setOOBInline(false);

        this.getSocketChannel().configureBlocking(false);
        connectorThread.register(this.getSocketChannel(), SelectionKey.OP_READ);//注册事件

        CommunicationRequestHttpFirst communicationHttpFirst = new CommunicationRequestHttpFirst(CommunicationEnum.HTTP);
        communicationHttpFirst.setHost(clientConfig.getHostName());
        writeN(createCommunicationRequest(communicationHttpFirst));
    }


    /**
     * 创建客户端与服务端交互器
     */
    public void createCommunicationProcess() {
        CommunicationProcessor communicationProcessor = new CommunicationProcessor(this, serviceConnectorThread);
        try {
            this.connectorThread = new ConnectorThread(communicationProcessor, "clientCommunication-thread");
            connectorThread.start();            /*启动当前连接器*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
