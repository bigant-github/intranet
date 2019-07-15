package priv.bigant.intranet.client;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.Connector;
import priv.bigant.intrance.common.ProcessBase;
import priv.bigant.intrance.common.SocketBean;
import priv.bigant.intrance.common.communication.CommunicationRequest;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class HttpIntranetConnectorProcess extends ProcessBase {

    private ByteBuffer byteBuffer;
    private ClientConfig clientConfig;

    private Connector.ConnectorThread serviceConnectorThread;
    private static final Logger LOG = LoggerFactory.getLogger(HttpIntranetConnectorProcess.class);

    public HttpIntranetConnectorProcess(Connector.ConnectorThread serviceConnectorThread) {
        this.byteBuffer = ByteBuffer.allocate(1024);
        clientConfig = (ClientConfig) ClientConfig.getConfig();
        this.serviceConnectorThread = serviceConnectorThread;
    }

    @Override
    public void start() {

    }

    @Override
    public void read(Connector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
        byteBuffer.clear();
        int read = socketChannel.read(byteBuffer);
        byteBuffer.flip();
        CommunicationRequest.CommunicationRequestHttpAdd communicationRequestHttpAdd = CommunicationRequest.createCommunicationRequest(ArrayUtils.subarray(byteBuffer.array(), 0, read)).toJavaObject(CommunicationRequest.CommunicationRequestHttpAdd.class);
        String id = communicationRequestHttpAdd.getId();

        //创建http accept
        SocketChannel serviceChannel = SocketChannel.open(new InetSocketAddress(clientConfig.getDomainName(), clientConfig.getHttpAcceptPort()));
        SocketBean socketBean = new SocketBean(serviceChannel);
        CommunicationRequest.CommunicationRequestHttpAdd communicationRequestHttpAdd1 = new CommunicationRequest.CommunicationRequestHttpAdd();
        communicationRequestHttpAdd1.setId(id);
        CommunicationRequest type = null;
        try {
            type = CommunicationRequest.createCommunicationRequest(communicationRequestHttpAdd1);
        } catch (Exception e) {
            LOG.error("create accept connector error", e);
            socketBean.close();
            return;
        }
        byteBuffer.clear();
        byteBuffer.put(type.toByte());
        byteBuffer.flip();
        socketChannel.write(byteBuffer);
        serviceConnectorThread.register(socketChannel, SelectionKey.OP_READ, id);
    }

    @Override
    public void accept(Connector.ConnectorThread connectorThread, SelectionKey selectionKey) throws IOException {
        //没有连接
    }

    @Override
    public String getName() {
        return "";
    }
}
