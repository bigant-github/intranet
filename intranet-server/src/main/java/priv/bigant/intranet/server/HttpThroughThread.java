package priv.bigant.intranet.server;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import priv.bigant.intrance.common.CodeEnum;
import priv.bigant.intrance.common.CommunicationRequest;
import priv.bigant.intrance.common.CommunicationResponse;
import priv.bigant.intrance.common.SocketBeanss;
import priv.bigant.intrance.common.http.HttpSocketManager;

import java.io.IOException;
import java.net.Socket;

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
            socketBean.setDomainName(host);
            boolean exist = HttpSocketManager.isExist(host);
            if (!exist) {
                HttpSocketManager.add(socketBean);
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

}
