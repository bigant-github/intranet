package priv.bigant.intranet.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.*;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

/**
 * Created by GaoHan on 2018/5/22.
 */
public class HttpThroughThread extends Thread {
    // 和本线程相关的Socket
    private SocketBeanss socketBean;

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpThroughThread.class);
    private ServerCommunication serverCommunication;

    public HttpThroughThread(Socket socket) throws IOException {
        this.socketBean = new SocketBeanss(socket);
    }

    @Override
    public void run() {
        try {
            serverCommunication = new ServerCommunication(socketBean.getSocket());
            CommunicationRequest.CommunicationRequestHttpFirst communicationRequestHttpFirst = serverCommunication.readRequest().toJavaObject(CommunicationRequest.CommunicationRequestHttpFirst.class);
            String host = communicationRequestHttpFirst.getHost();
            serverCommunication.setHost(host);
            socketBean.setDomainName(host);
            boolean exist = HttpSocketManager.isExist(host);
            if (!exist) {
                HttpSocketManager.add(host, serverCommunication);
                serverCommunication.write(CommunicationResponse.createSuccess());
                LOGGER.info(host + " 连接成功");
            } else {
                serverCommunication.write(CommunicationResponse.create(CodeEnum.HOST_ALREADY_EXIST));
                LOGGER.info(host + CodeEnum.HOST_ALREADY_EXIST.getMsg());
            }
        } catch (IOException e) {
            LOGGER.error("connection io error", e);
            serverCommunication.close();
        } catch (Exception e) {
            LOGGER.error("connection error", e);
            serverCommunication.close();
        }
    }

    public void createdSocketBean() {
        for (int x = 0; x < 5; x++) {
            String id = UUID.randomUUID().toString();
            CommunicationRequest communicationRequest = null;
            CommunicationRequest.CommunicationRequestP communicationRequestHttpAdd = new CommunicationRequest.CommunicationRequestHttpAdd(id);
            try {
                communicationRequest = CommunicationRequest.createCommunicationRequest(communicationRequestHttpAdd);
            } catch (Exception e) {
                e.printStackTrace();
            }
            HttpSocketManager.addKey(id, serverCommunication.getHost());
            serverCommunication.write(communicationRequest);
        }
    }

}
