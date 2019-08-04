package priv.bigant.intranet.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.*;
import priv.bigant.intrance.common.ServerConnector.ConnectorThread;
import priv.bigant.intrance.common.communication.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * @author GaoLei 保持客户端与服务端通信
 */
public class ClientCommunication extends Communication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCommunication.class);
    private ClientConfig clientConfig;
    private ByteBuffer byteBuffer;
    private ConnectorThread serviceConnectorThread;

    public ClientCommunication(ConnectorThread serviceConnectorThread) {
        this.serviceConnectorThread = serviceConnectorThread;
        clientConfig = (ClientConfig) ClientConfig.getConfig();
        byteBuffer = ByteBuffer.allocate(clientConfig.getCommunicationByteBufferSize());
        /*HttpIntranetServiceProcess httpIntranetServiceProcess = new HttpIntranetServiceProcess();
        try {
            serviceConnectorThread = new ServerConnector.ConnectorThread(httpIntranetServiceProcess);
            serviceConnectorThread.start();
        } catch (IOException e) {
            LOGGER.error("http 处理器启动失败");
        }*/
    }


    public void connect() throws Exception {
        this.socketChannel = SocketChannel.open(new InetSocketAddress(clientConfig.getHostName(), clientConfig.getPort()));
        socketChannel.socket().setKeepAlive(true);
        socketChannel.socket().setOOBInline(false);
        CommunicationRequest.CommunicationRequestHttpFirst communicationHttpFirst = new CommunicationRequest.CommunicationRequestHttpFirst(CommunicationEnum.HTTP);
        communicationHttpFirst.setHost(clientConfig.getDomainName());
        writeN(CommunicationRequest.createCommunicationRequest(communicationHttpFirst));
    }

    @Override
    public void run() {
        //while (true) {//监控是否断开
        try {
            CommunicationResponse.CommunicationResponseP communicationResponseP = null;
            connect();

            if (!(socketChannel != null && socketChannel.isConnected())) {
                LOGGER.error("连接服务器失败");
            }

            //new CommunicationListener(this).start();
            if (communicationResponseP.isSuccess()) {
                LOGGER.info("connect success:host=" + clientConfig.getDomainName());
                while (true) {
                    CommunicationRequest communicationRequest = readRequest();
                    CommunicationEnum type = communicationRequest.getType();
                    if (type.equals(CommunicationEnum.HTTP_ADD)) {
                        add(communicationRequest);
                    }
                }
            }
            CodeEnum code = communicationResponseP.getCode();
            if (CodeEnum.HOST_ALREADY_EXIST.equals(code)) {
                LOGGER.error("connect error:" + code.getMsg());
            }
        } catch (IOException e) {
            LOGGER.error("connect error: host =" + clientConfig.getDomainName() + "     try connect    ", e);
            super.close();
            try {
                sleep(10000);
            } catch (InterruptedException ignored) {
                ;
            }
        } catch (Exception e) {
            super.close();
            LOGGER.error("连接失败", e);
        }
        //}
    }

    /**
     * 创建http通信连接
     *
     * @param communicationRequest
     */
    public void add(CommunicationRequest communicationRequest) {
        SocketChannel socketChannel;
        CommunicationRequest.CommunicationRequestHttpAdd communicationRequestHttpAdd = communicationRequest.toJavaObject(CommunicationRequest.CommunicationRequestHttpAdd.class);
        String id = communicationRequestHttpAdd.getId();
        try {
            socketChannel = SocketChannel.open(new InetSocketAddress(clientConfig.getHostName(), clientConfig.getHttpAcceptPort()));
            socketChannel.socket().setKeepAlive(true);
            CommunicationRequest.CommunicationRequestHttpAdd communicationRequestHttpAdd1 = new CommunicationRequest.CommunicationRequestHttpAdd();
            communicationRequestHttpAdd1.setId(id);
            CommunicationRequest type = CommunicationRequest.createCommunicationRequest(communicationRequestHttpAdd1);
            byteBuffer.clear();
            byteBuffer.put(type.toByte());
            byteBuffer.flip();
            socketChannel.write(byteBuffer);
            CommunicationResponse communicationResponse = CommunicationResponse.createCommunicationResponse(new CommunicationResponse.CommunicationResponseHttpAdd(id));
            writeN(communicationResponse);
            socketChannel.configureBlocking(false);
            serviceConnectorThread.register(socketChannel, SelectionKey.OP_READ);
        } catch (Exception e) {
            LOGGER.error("add http socket error", e);
            //write(new CommunicationResponse(CodeEnum.ERROR));
        }
    }
}
