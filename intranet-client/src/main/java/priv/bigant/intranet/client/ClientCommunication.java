package priv.bigant.intranet.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ClientCommunication extends Communication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientCommunication.class);
    private ClientConfig clientConfig;

    public Socket getSocket() {
        return socket;
    }

    public ClientCommunication() {
        clientConfig = (ClientConfig) ClientConfig.getConfig();
    }


    @Override
    public void connect() throws Exception {
        try {
            if (socket != null) {
                close();
            }
            this.socket = new Socket();
            socket.connect(new InetSocketAddress(clientConfig.getHostName(), clientConfig.getPort()));
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();

            CommunicationRequest.CommunicationRequestHttpFirst communicationHttpFirst = new CommunicationRequest.CommunicationRequestHttpFirst(CommunicationEnum.HTTP);
            communicationHttpFirst.setHost(clientConfig.getDomainName());

            write(CommunicationRequest.createCommunicationRequest(communicationHttpFirst));

            CommunicationResponse.CommunicationResponseP communicationResponseP = readResponse().toJavaObject(CommunicationResponse.CommunicationResponseP.class);

            if (!communicationResponseP.isSuccess()) {
                LOGGER.error("connect error ");
            }

            LOGGER.info("connect success:host=" + clientConfig.getDomainName());
        } catch (Exception e) {
            LOGGER.error("connect error: host =" + clientConfig.getDomainName(), e);
            throw e;
        }
    }

    @Override
    public void run() {
        try {
            connect();
        } catch (Exception e) {
            LOGGER.error("连接失败", e);
            return;
        }
        while (true) {
            CommunicationRequest communicationRequest = readRequest();
            CommunicationEnum type = communicationRequest.getType();
            if (type.equals(CommunicationEnum.HTTP_ADD)) {
                add(communicationRequest);
            }
        }
    }

    public void add(CommunicationRequest communicationRequest) {
        SocketBeanss socketBeanss = null;
        CommunicationRequest.CommunicationRequestHttpAdd communicationRequestHttpAdd = communicationRequest.toJavaObject(CommunicationRequest.CommunicationRequestHttpAdd.class);
        String id = communicationRequestHttpAdd.getId();
        try {
            Socket socket = new Socket(clientConfig.getHostName(), clientConfig.getLocalPort());
            socketBeanss = new SocketBeanss(socket);
            CommunicationRequest.CommunicationRequestHttpAdd communicationRequestHttpAdd1 = new CommunicationRequest.CommunicationRequestHttpAdd();
            communicationRequestHttpAdd1.setId(id);
            CommunicationRequest type = CommunicationRequest.createCommunicationRequest(communicationRequestHttpAdd1);
            socketBeanss.getOs().write(type.toByte());
        } catch (Exception e) {
            LOGGER.error("add http socket error", e);
            if (socketBeanss != null)
                socketBeanss.close();
            //write(new CommunicationResponse(CodeEnum.ERROR));
        }

        /*try {//成功返回
            CommunicationResponse communicationResponse = CommunicationResponse.createCommunicationResponse(new CommunicationResponse.CommunicationResponseHttpAdd(id));
            write(communicationResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

}
